package io.fairyproject.gradle;

import io.fairyproject.gradle.compile.ModuleCompilerAction;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;

public class ProjectCompilePlugin implements Plugin<Project> {

    private SourceSetContainer sourceSets;

    @Override
    public void apply(Project project) {
        this.sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
        project.getPlugins().withType(JavaPlugin.class, plugin -> this.configurePlugin(project, "java"));
    }

    private void configurePlugin(Project project, String language) {
        sourceSets.all(sourceSet -> {
            project.getTasks().named(sourceSet.getCompileTaskName(language), AbstractCompile.class, compile -> {
                ModuleCompilerAction action = project.getObjects().newInstance(ModuleCompilerAction.class);
                compile.doLast("moduleCompile", action);
            });
        });
    }
}
