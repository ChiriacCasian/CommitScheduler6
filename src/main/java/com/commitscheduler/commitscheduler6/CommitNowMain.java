package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;

import java.io.*;
import java.lang.ProcessBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class CommitNowMain {
    private static String remoteBranchName ;
    private static String localBranchName ;
    private static String pat_token ;
    private static String ssh_key ;
    private static Project project ;
    public static void main(PersistanceStateVariables state, Project localProject) throws IOException, InterruptedException {//1234
        project = localProject ;
        getProperties(state) ;
        connectToSsh();
        //checkProtocol() ;
//        int commitStackSize = commitStackSizeCalculator() ;
//        if(commitStackSize == 0){
//            System.out.println("Nothing to push, branch up to date");
//            return ;
//        }
        String oldestCommit = getOldestUnpushedCommit();
        String freshestCommit = getFreshestCommit();
        System.out.println("there are oldest + freshest : " + oldestCommit + " " + freshestCommit);
        runCommand("git log origin/main..main --pretty=format:\"%H %s\"");
        if(freshestCommit == null || oldestCommit == null){
            System.out.println("Nothing to push, branch up to date");
            return ;
        }
        setHeadToSha1(oldestCommit);
        push() ;
        setHeadToSha1(freshestCommit);
//        System.out.println(commitStackSize + ": " + commitStackSizeCalculator());
//        if(commitStackSizeCalculator() == commitStackSize){
//            System.out.println("Push failed, please check your ssh_key and/or PAT token and try again");
//            return ;
//        }
    }
    private static void runCommand(String command) {
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        processBuilder.inheritIO(); ///////////////////////////
        processBuilder.command(command.split(" "));
        try {
            Process process = processBuilder.start();
            process.waitFor() ;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static int commitStackSizeCalculator() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> command = Arrays.asList("git", "log", remoteBranchName + "/" + localBranchName + ".." +
                localBranchName, "--pretty=format:%H");
        processBuilder.command(command) ;
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "" ;
        int rez = 0 ;
        while((line = reader.readLine()) != null){
            rez ++ ;
        }
        return rez ;
    }

    private static void checkProtocol() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> command = Arrays.asList("git", "config", "--get", "remote."+remoteBranchName +".url") ;

        processBuilder.command(command) ;
        Process process = processBuilder.start();
        process.waitFor() ;

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String remoteLink = reader.readLine() ;
        if(remoteLink.startsWith("https://github.com") || changedToken(remoteLink)){
            /// otherwise it means either ssh is configured, or PAT token is provided
            /// either switch to ssh, or use PAT token to authenticate http remote communication
            /// if PAT token is provided in the properties file, use it to authenticate
            if(pat_token == null){
                System.out.println("You are using http protocol but no authentication token provided, exiting");
                System.out.println("Please provide a PAT token in the application.properties file, or switch to ssh protocol");
                return ;
            }
            /// pat token migh still be wrong, or not have the required permissions
            if(ssh_key != null){
                connectToSsh() ;
                return;
            }
            connectToHttp(remoteLink);
        }
        /// they are using ssh so everything is fine
        return ;
    }
    private static void connectToSsh() throws IOException, InterruptedException {
        if(ssh_key == null)throw new RuntimeException("You are using ssh protocol but no ssh key provided, exiting") ;
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        processBuilder.inheritIO(); ///////////////////////////
        List<String> command = Arrays.asList("git", "remote", "set-url", remoteBranchName, ssh_key) ;
        processBuilder.command(command).start().waitFor() ;
    }
    private static void connectToHttp(String remoteLink) throws IOException {
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO(); ///////////////////////////
        List<String> command ;
        if(!changedToken(remoteLink))
            command = Arrays.asList("git", "remote", "set-url", remoteBranchName, remoteLink.replace("https://", "https://"+pat_token+"@")) ;
        else {
            remoteLink += "/" ;
            String[] oldRemoteLinkComposition = remoteLink.split("/") ;
            String newRemoteLink = "" ;
            for(int i = 0; i < oldRemoteLinkComposition.length ; i++){
                if(i != 2)newRemoteLink += oldRemoteLinkComposition[i] + "/" ;
                else newRemoteLink += pat_token + "@github.com" + "/" ;
            }
            newRemoteLink = newRemoteLink.substring(0, newRemoteLink.length() - 1) ;
            command = Arrays.asList("git", "remote", "set-url", remoteBranchName, newRemoteLink);
        }
        processBuilder.command(command).start() ;
    }
    private static void push() throws IOException, InterruptedException {
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        //processBuilder.inheritIO(); ///////////////////////////
        List<String> command = Arrays.asList("git", "push", remoteBranchName, localBranchName) ;
        processBuilder.command(command).start().waitFor() ;
    }
    private static String getOldestUnpushedCommit() throws IOException, InterruptedException {
        //git log origin/main..main --pretty=format:"%H %s"
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        List<String> command = Arrays.asList("git", "log", remoteBranchName + "/" + localBranchName + ".." +
                localBranchName, "--pretty=format:%H");
        processBuilder.command(command) ;
        Process process = processBuilder.start();
        process.waitFor() ;

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "" ;
        String rez = null ;
        while((line = reader.readLine()) != null){
            rez = line ;
        }
        return rez ;
    }
    private static String getFreshestCommit() throws IOException, InterruptedException {
        //git log origin/main..main --pretty=format:"%H %s"
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        List<String> command = Arrays.asList("git", "log", remoteBranchName + "/" + localBranchName + ".." +
                localBranchName, "--pretty=format:%H");
        processBuilder.command(command) ;
        Process process = processBuilder.start();
        process.waitFor() ;
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.readLine() ;
    }
    private static void setHeadToSha1(String sha1) throws IOException, InterruptedException {
        runCommand("git rev-parse HEAD");
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        List<String> command = Arrays.asList("git", "reset", "--soft", sha1) ;
        processBuilder.command(command).start().waitFor() ;
        runCommand("git rev-parse HEAD");
    }
    private static boolean changedToken(String remoteLink) {
        return pat_token != null && !remoteLink.contains(pat_token) ;
    }
    private static void getProperties(PersistanceStateVariables state) throws IOException {
        remoteBranchName = state.getRemoteBranchName();
        localBranchName = state.getLocalBranchName();
        pat_token = state.getPatToken();
        ssh_key = state.getSshKey();
    }
}