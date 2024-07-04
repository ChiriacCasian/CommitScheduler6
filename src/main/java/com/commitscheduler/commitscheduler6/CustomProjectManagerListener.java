package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepositoryManager;

public class CustomProjectManagerListener implements ProjectManagerListener {
    @Override
    public void projectClosing(Project project) {
        GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(project);
        MessageBusConnection connection = project.getMessageBus().connect();

        ConfigPersistangeManager settings = ConfigPersistangeManager.getInstance(project);
        settings.getState().saveDayList();
        System.out.println("Project closing saving state");
    }
}
