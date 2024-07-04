package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import git4idea.GitCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    Project project;
    PersistanceStateVariables state;
    private final ScheduledExecutorService scheduler;
    public Scheduler(Project project, PersistanceStateVariables state) {
        this.project = project;
        this.state = state;
        this.scheduler = Executors.newScheduledThreadPool(1) ;
        System.out.println("Scheduler created !!");
        commitPushingTicker();
    }
    public void addCommit(CommitObject commit) {
        state.addCommit(commit);
    }
    public LocalDateTime getNextGoodTime(){
        return LocalDateTime.now().plusMinutes(state.getDelayCommitPush()) ; /// default time to push currently is set to 10 minutes
    }
    public void commitPushingTicker() { /// HERE CONFIGURE TICKER FREQUENCY
        scheduler.scheduleAtFixedRate(pushDueCommits(), 0, 20, TimeUnit.SECONDS);
    }
    private Runnable pushDueCommits() {
        return () -> {
            System.out.println("pushing due Commits !! /////////////////");
            state.updateDayList();
            state.sortCommitsAfterDate();
            List<CommitObject> commitsList = new ArrayList<>(state.getCommits()) ;
            System.out.println(state.getCommits().stream().map(i -> i.getCommitMessage()).toList());
            for (int f = 0 ; f < commitsList.size() ; f++) {
                CommitObject commit = commitsList.get(f);
                System.out.println(commit.getCommitMessage() + " " + f);
                try {
                    commit.push();
                    System.out.println(state.getCommits().stream().map(i -> i.getCommitMessage()).toList());
                    state.updateDayList();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to push commit");
                }
            }
        };
    }
}
