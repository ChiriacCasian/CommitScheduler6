package com.commitscheduler.commitscheduler6.Frontend;

import com.commitscheduler.commitscheduler6.ConfigPersistangeManager;
import com.commitscheduler.commitscheduler6.PersistanceStateVariables;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SimpleAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event){
        Project project = event.getProject();
        if (project == null) return;

        ConfigPersistangeManager settings = ConfigPersistangeManager.getInstance(project);
        PersistanceStateVariables state = settings.getState();
        if (state == null) {
            System.out.println("State is null ???");
            return;
        }

        // Initialize the dialog with current settings
        ConfigDialog dialog = null;
        try {
            dialog = new ConfigDialog(state, project);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("we here " + state.getHttpLink() + " " + state.getSshKey());
        if (dialog.showAndGet()) {
            // If the user clicked "OK", save the configuration
            state.setPatToken(dialog.getPatToken());
            state.setSshKey(dialog.getSshInputedKey());
        }
    }

}
