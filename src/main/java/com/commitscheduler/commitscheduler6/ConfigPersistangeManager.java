package com.commitscheduler.commitscheduler6;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
@State(
        name = "com.commitscheduler.commitscheduler6.PersistentStateVariables",
        storages = {@Storage("PersistentStateVariables.xml")}
)
public class ConfigPersistangeManager implements PersistentStateComponent<PersistanceStateVariables> {
    private PersistanceStateVariables state = new PersistanceStateVariables();
    @Nullable
    @Override
    public PersistanceStateVariables getState() {
        return state;
    }
    @Override
    public void loadState(@NotNull PersistanceStateVariables state) {
        this.state = state;
    }
    public static ConfigPersistangeManager getInstance(@NotNull com.intellij.openapi.project.Project project) {
        return project.getService(ConfigPersistangeManager.class);
    }
}