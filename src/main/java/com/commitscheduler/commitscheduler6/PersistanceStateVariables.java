package com.commitscheduler.commitscheduler6;

public class PersistanceStateVariables {
    private String patToken = "default PAT token";
    private String sshKey = "default ssh key";
    private String remoteBranchName = "usually origin";
    private String localBranchName = "usually main or master";

    public String getPatToken() {
        return patToken;
    }

    public void setPatToken(String patToken) {
        this.patToken = patToken;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    public String getRemoteBranchName() {
        return remoteBranchName;
    }

    public void setRemoteBranchName(String remoteBranchName) {
        this.remoteBranchName = remoteBranchName;
    }

    public String getLocalBranchName() {
        return localBranchName;
    }

    public void setLocalBranchName(String localBranchName) {
        this.localBranchName = localBranchName;
    }
}
