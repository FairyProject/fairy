package io.fairyproject.gradle;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class IDEDependencyLookup {

    private Map<String, String> NAME_TO_PROJECT_IDENTITY;

    public void init(Project rootProject) {
        NAME_TO_PROJECT_IDENTITY = new HashMap<>();

        for (Project project : rootProject.getAllprojects()) {
            if (!(project instanceof ProjectInternal)) {
                continue;
            }

            NAME_TO_PROJECT_IDENTITY.put(project.getName(), ((ProjectInternal) project).getIdentityPath().toString());
        }
    }

    public String getIdentityPath(String name) {
        return NAME_TO_PROJECT_IDENTITY.get(name);
    }

}
