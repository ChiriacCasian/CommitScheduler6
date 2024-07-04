package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import git4idea.GitBranch;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConfigStateOnStartup {
    public static void configStateOnStartup(Project project) throws IOException, InterruptedException {
        ConfigPersistangeManager settings = ConfigPersistangeManager.getInstance(project);
        PersistanceStateVariables state = settings.getState();
        /// go through all branches and check if there are any commits that are not pushed and are not
        /// in the state variable
        GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(project);

        for (GitRepository repository : repositoryManager.getRepositories()) {
            for (GitLocalBranch localBranch : repository.getBranches().getLocalBranches()){
                GitBranchTrackInfo trackInfo = findTrackInfoForBranch(repository, localBranch);
                if (trackInfo != null) {
                    GitRemoteBranch remoteBranch = trackInfo.getRemoteBranch();

                    addToStateUnpushedCommits(project, localBranch, remoteBranch, state);
                    addToStateBranches(project, localBranch, remoteBranch, state);
                }
            }
        }
        state.sortCommitsAfterDate();
    }
    private static GitBranchTrackInfo findTrackInfoForBranch(GitRepository repository, GitBranch localBranch) {
        for (GitBranchTrackInfo trackInfo : repository.getBranchTrackInfos()) {
            if (trackInfo.getLocalBranch().equals(localBranch)) {
                return trackInfo;
            }
        }
        return null;
    }
    public static void addToStateUnpushedCommits(Project project, GitLocalBranch localBranch,
                                                 GitRemoteBranch remoteBranch,
                                                 PersistanceStateVariables state) throws IOException, InterruptedException {
        String sha1s = runCommand(project,
                "git log " + remoteBranch.getName() + ".." + localBranch.getName() + " --pretty=format:%H");

        List<String> sha1List = Arrays.stream(sha1s.split("\n")).toList();
        if(sha1List.isEmpty() || (sha1List.size() == 1 && sha1List.get(0).isEmpty())){
            state.removeAllCommits() ;
            return ;
        }
        for(String sha1 : sha1List){
            CommitObject potentialCommit = getCommitBySha1(project, sha1, remoteBranch.getName(), localBranch.getName(), state);
            state.addCommit(potentialCommit);
        }
    }
    public static void addToStateBranches(Project project, GitLocalBranch localBranch,
                                          GitRemoteBranch remoteBranch,
                                          PersistanceStateVariables state) {
        state.addBranch(remoteBranch.getName(), localBranch.getName());
    }
    public static CommitObject getCommitBySha1(Project project, String commitSha1, String remoteBranchName, String localBranchName, PersistanceStateVariables state) throws IOException, InterruptedException {
        String outputLine = runCommand(project,
                "git show --pretty=format:%an%n%ae%n%at%n%s%n%b -s " + commitSha1);

        if (outputLine != null && !outputLine.isEmpty()) {
            return new CommitObject(commitSha1, outputLine.split("\n")[3], state, project,
                    LocalDateTime.now().plusMinutes(5),
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(outputLine.split("\n")[2])), ZoneId.systemDefault()),
                    remoteBranchName.split("/")[0], localBranchName);
        } else {
            return null; // Commit not found
        }
    }
    public static String runCommand(Project project, String command) throws IOException, InterruptedException {
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
            rez += '\n' ;
        }
        return rez;
    }
}
