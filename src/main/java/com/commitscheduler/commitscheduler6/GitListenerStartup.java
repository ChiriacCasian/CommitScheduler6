package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.vcs.log.ui.table.column.Commit;
import git4idea.*;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitConfig;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.BuiltInServerManager;
import com.intellij.openapi.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GitListenerStartup implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(project);
        MessageBusConnection connection = project.getMessageBus().connect();

        ConfigPersistangeManager settings = ConfigPersistangeManager.getInstance(project);
        settings.getState().loadDayList();
        System.out.println("Project opening loading state");

        try {
            ConfigStateOnStartup.configStateOnStartup(project) ;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (GitRepository repository : repositoryManager.getRepositories()) {
            connection.subscribe(GitRepository.GIT_REPO_CHANGE, new CommitListener(project));
        }
    }
}
