package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import com.commitscheduler.commitscheduler6.ConfigDialog;

import javax.swing.plaf.synth.SynthTextAreaUI;

public class SimpleAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        ConfigPersistangeManager settings = ConfigPersistangeManager.getInstance(project);
        PersistanceStateVariables state = settings.getState();
        if (state == null) {
            System.out.println("State is null ???");
            return;
        }

        // Initialize the dialog with current settings
        ConfigDialog dialog = new ConfigDialog(state, project);

        if (dialog.showAndGet()) {
            // If the user clicked "OK", save the configuration
            state.setPatToken(dialog.getConfigItem1());
            state.setSshKey(dialog.getConfigItem2());
            state.setRemoteBranchName(dialog.getConfigItem3());
            state.setLocalBranchName(dialog.getConfigItem4());
        }
    }

}
