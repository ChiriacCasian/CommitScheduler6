package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.apache.batik.ext.awt.image.renderable.PaintRable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ConfigGitProtocol {
    private static String pat_token;
    private static String ssh_key;
    private static Project project;
    private static boolean gitProtocol; /// 1 means ssh, 0 means https
    private static String remoteBranchName;

    public static void main(Project project1,
                            PersistanceStateVariables state,
                            String remoteBranchName1) throws IOException, InterruptedException {
        getProperties(state);
        project = project1;
        remoteBranchName = remoteBranchName1;
        if (gitProtocol) connectToSsh();
        else connectToHttp();
    }
    public static boolean getProjectProtocol(Project lProject,
                                             String remoteBranchName) throws IOException, InterruptedException {
        String remoteLink = getLink(lProject, remoteBranchName);

        if(remoteLink.startsWith("https:")) return false ;
        return true ;
    }
    public static String getLink(Project project1, String remoteBranchName) throws IOException, InterruptedException {
        project = project1;
        String outputLine = runCommand("git remote get-url " + remoteBranchName);
        return outputLine ;
    }
    public static void setProtocol(boolean gitProtocol, PersistanceStateVariables state, Project lProject) throws IOException, InterruptedException {
        getProperties(state);
        project = lProject;
        remoteBranchName = state.getBranches().get(0).getFirst().split("/")[0];
        if (gitProtocol) connectToSsh(); /// 1 means ssh
        else connectToHttp();
    }
    private static void connectToSsh() throws IOException, InterruptedException {
        if (ssh_key == null) throw new RuntimeException("You are using ssh protocol but no ssh key provided, exiting");
        runCommand("git remote set-url " + remoteBranchName + " " + ssh_key);
    }
    private static void connectToHttp() throws IOException, InterruptedException {
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));

        List<String> command = Arrays.asList("git", "config", "--get", "remote." + remoteBranchName + ".url");

        processBuilder.command(command);
        Process process = processBuilder.start();
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String remoteLink = reader.readLine();
//        if(remoteLink == null){
//            String aux = runCommand("git rev-parse --show-toplevel") ;
//            aux = "git config --global --add safe.directory " + aux ;
//            System.out.println(aux);
//            runCommand("git config --global --add safe.directory " ) ;
//            connectToHttp();
//            return;
//        }
        if (remoteLink.startsWith("https://")) {
            if (pat_token == null)
                throw new RuntimeException("You are using https protocol but no PAT token provided, exiting");
            if (remoteLink.contains(pat_token)) return; /// already using the token, no need to change anything
            /// if it just contains the pat_token and it contains it twice, or it has some chararcters before or after
            // the token the whole plugin ~-FAILS IRREPARABLY-~
            String httpsCommand;
            if (remoteLink.startsWith("https://github.com"))
                httpsCommand = "git remote set-url " + remoteBranchName + " " + remoteLink.replace("https://", "https://" + pat_token + "@");
            else { /// this handles if the token has changed and needs to be updated
                remoteLink += "/";
                String[] oldRemoteLinkComposition = remoteLink.split("/");
                String newRemoteLink = "";
                for (int i = 0; i < oldRemoteLinkComposition.length; i++) {
                    if (i != 2) newRemoteLink += oldRemoteLinkComposition[i] + "/";
                    else newRemoteLink += pat_token + "@github.com" + "/";
                }
                newRemoteLink = newRemoteLink.substring(0, newRemoteLink.length() - 1);
                httpsCommand = "git remote set-url " + remoteBranchName + " " + newRemoteLink;
            }
            runCommand(httpsCommand);
        } else { /// currently using ssh, switch to using https
            remoteLink = remoteLink.replace(':', '/');

            String sshCommand = "git remote set-url " + remoteBranchName + " " + remoteLink.replace("git@", "https://" + pat_token + "@");
            runCommand(sshCommand);
        }
    }
    public static Pair<String, Boolean> testConnection(Project project1) throws IOException, InterruptedException {
        project = project1 ;
        String outputLine = runCommandErrorReader("git push --dry-run");
        if(outputLine.contains("fatal:")){
            return new Pair<String, Boolean>(outputLine, false);
        }
        if(outputLine.startsWith("Everything") || outputLine.startsWith("To "))return new Pair<String, Boolean>(outputLine, true);
        return new Pair<>(outputLine, false); /// any other case like invalid https PAT for example
    }
    private static String runCommand(String command) throws IOException, InterruptedException {
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        processBuilder.command(command.split(" "));
        Process process = processBuilder.start();
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        String rez = "";
        while ((line = reader.readLine()) != null) {
            rez += line;
        }
        return rez;
    }
    private static String runCommandErrorReader(String command) throws IOException, InterruptedException {
        String projectDirectory = project.getBasePath(); // Ensure this is properly initialized
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        processBuilder.command(command.split(" "));
        Process process = processBuilder.start();

        StringBuilder error = new StringBuilder();
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = errorReader.readLine()) != null) {
            error.append(line).append("\n");
        }

        process.waitFor();

        // Return only the error stream
        return error.toString();
    }
    private static void getProperties(PersistanceStateVariables state) {
        pat_token = state.getPatToken();
        ssh_key = state.getSshKey();
        gitProtocol = state.getProtocol();
    }
}
