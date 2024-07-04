package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import git4idea.GitCommit;

import java.io.IOException;
import java.time.LocalDateTime;

public class CommitObject {
    private String sha1 ;
    private String commitMessage;
    private PersistanceStateVariables state ;
    private Project project ;
    private LocalDateTime scheduledPushTime ;
    private LocalDateTime commitTime ;
    private String remoteBranchName;
    private String localBranchName;

    public CommitObject(String sha1, String commitMessage, PersistanceStateVariables state, Project project,
                        LocalDateTime scheduledPushTime, LocalDateTime commitTime, String remoteBranchName, String localBranchName) {
        this.sha1 = sha1;
        this.commitMessage = commitMessage;
        this.state = state;
        this.project = project;
        this.scheduledPushTime = scheduledPushTime;
        this.commitTime = commitTime;
        this.remoteBranchName = remoteBranchName;
        this.localBranchName = localBranchName;
    }
    /// 1 means it was successfull -1 means it failed
    public int push() throws IOException, InterruptedException {
        /// it should only acutally push if the nrOfCommits of the current day is more than 0
        if(state.getNrOfCommitsToday() <= 0){
            System.out.println("Scheduled pushes limit reached for today");
            return 0 ;
        }
        if(CommitNowMain.main(state, project, remoteBranchName, localBranchName) == 1) {
            state.removeCommit(this);
            return 1 ;
        }
        return -1 ;
    }
    public PersistanceStateVariables getState() {
        return state;
    }

    public Project getProject() {
        return project;
    }

    public LocalDateTime getScheduledPushTime() {
        return scheduledPushTime;
    }

    public String getRemoteBranchName() {
        return remoteBranchName;
    }

    public String getLocalBranchName() {
        return localBranchName;
    }

    public String getCommitMessage() {
        return commitMessage;
    }
    public String getCommitSha1(){
        return sha1 ;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public void setState(PersistanceStateVariables state) {
        this.state = state;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setScheduledPushTime(LocalDateTime scheduledPushTime) {
        this.scheduledPushTime = scheduledPushTime;
    }

    public LocalDateTime getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(LocalDateTime commitTime) {
        this.commitTime = commitTime;
    }

    public void setRemoteBranchName(String remoteBranchName) {
        this.remoteBranchName = remoteBranchName;
    }

    public void setLocalBranchName(String localBranchName) {
        this.localBranchName = localBranchName;
    }
    public String toString(){
        return commitMessage + " " +
                commitTime.toString() + " " + scheduledPushTime.toString() + " " +
                remoteBranchName + " " + localBranchName ;
    }
    public boolean equals(Object o){
        if(o.getClass().equals(CommitObject.class)){
            CommitObject c = (CommitObject) o;
            return c.getCommitSha1().equals(this.sha1) ;
        }
        return false;
    }
}
