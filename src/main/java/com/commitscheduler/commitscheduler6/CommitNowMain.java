package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import git4idea.GitCommit;

import javax.swing.plaf.nimbus.State;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.*;
import java.lang.ProcessBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class CommitNowMain {
    private static String remoteBranchName;
    private static String localBranchName;
    private static Project project;
    /// debug codes :
    /// 1 means successfully pushed, 0 means nothing to push, -1 means push failed
    public static int main(PersistanceStateVariables state, Project localProject,
                            String remoteBranchName1, String localBranchName1) throws IOException, InterruptedException {//1234
        /**
         * in order to manage remote and local branches we will create an action on the commit button (and will have to track the terminal for commits aswell)
         * and when a commit occurs we will log the branches into a CommitConfig object which will be added to a stack of CommitConfigs
         */
        System.out.println(remoteBranchName1 + " " + localBranchName1);
        project = localProject;
        remoteBranchName = remoteBranchName1;
        localBranchName = localBranchName1;
        Pair<String, Boolean> connectionStatus = ConfigGitProtocol.testConnection(project);
        if(!connectionStatus.getSecond()) {
            System.out.println("Connection failed, please check your ssh_key and/or PAT token and try again");
            return -1;
        }

        String oldestCommit = getOldestUnpushedCommit();
        String freshestCommit = getFreshestCommit();
        if (freshestCommit == null || oldestCommit == null) {
            System.out.println("Nothing to push, branch up to date");
            return 0;
        }
        state.removeCommitBySha1(oldestCommit);
        runCommand("git reset --soft " + oldestCommit);
        runCommand("git push " + remoteBranchName + " " + localBranchName);
        runCommand("git reset --soft " + freshestCommit);
        state.registerPush() ;
        state.saveDayList();
        return 1 ;
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

    private static String getOldestUnpushedCommit() throws IOException, InterruptedException {
        //git log origin/main..main --pretty=format:"%H %s"
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        List<String> command = Arrays.asList("git", "log", remoteBranchName + "/" + localBranchName + ".." +
                localBranchName, "--pretty=format:%H");
        processBuilder.command(command);
        Process process = processBuilder.start();
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        String rez = null;
        while ((line = reader.readLine()) != null) {
            rez = line;
        }
        return rez;
    }

    private static String getFreshestCommit() throws IOException, InterruptedException {
        //git log origin/main..main --pretty=format:"%H %s"
        String projectDirectory = project.getBasePath(); ///////////////////
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectDirectory));
        List<String> command = Arrays.asList("git", "log", remoteBranchName + "/" + localBranchName + ".." +
                localBranchName, "--pretty=format:%H");
        processBuilder.command(command);
        Process process = processBuilder.start();
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }
}