package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vcs.VcsException;
import git4idea.GitCommit;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class CommitListener implements GitRepositoryChangeListener {

    private final Project project;
    private Scheduler scheduler;
    private PersistanceStateVariables state;

    public CommitListener(Project project) {
        this.project = project;
        ConfigPersistangeManager settings = ConfigPersistangeManager.getInstance(project);
        this.state = settings.getState() ; /// configure state here
        this.scheduler = new Scheduler(project, state);
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository repository) {
        /// HERE !!! remoteBranch is usually wrong bcs it has origin/main instead of origin
        /// this is called when a either a commit is made or a push is manually made etc
        String localBranch = repository.getCurrentBranchName();
        if (repository.getCurrentBranch() != null && repository.getCurrentBranch().findTrackedBranch(repository) != null) {
            String remoteBranch = repository.getCurrentBranch().findTrackedBranch(repository).getName();
            try {
                List<GitCommit> commits = GitHistoryUtils.history(project, repository.getRoot());
                if (!commits.isEmpty()) {
                    GitCommit latestCommit = commits.get(0);
                    scheduler.addCommit(new CommitObject(latestCommit.getId().asString(),
                            latestCommit.getFullMessage(), state, project, scheduler.getNextGoodTime(),
                            LocalDateTime.ofInstant(Instant.ofEpochSecond(latestCommit.getCommitTime()), ZoneId.systemDefault()),
                            trimBeginning(remoteBranch), localBranch)
                    ) ;
                    state.updateDayList();
                }
            } catch (VcsException e) {
                e.printStackTrace();
            }
        }
    }
    private static String trimBeginning(String branchName){
        /// if branch name is origin/main it will return origin, it returns everything that lies before /
        return branchName.split("/")[0];
    }
}
