package io.fairyproject.gradle;

import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class PublishSnapshotTask extends DefaultTask {

    @Setter
    private ModuleTask moduleTask;

    @TaskAction
    public void modifyVersion() {
        if (moduleTask != null)
            this.moduleTask.setSnapshot(true);
    }

}
