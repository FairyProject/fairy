package io.fairyproject.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.jvm.tasks.Jar;

public class ModulePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final ModuleExtension extension;
        try {
            extension = project.getExtensions().getByType(ModuleExtension.class);
        } catch (UnknownDomainObjectException ex) {
            return;
        }
        final ModuleTask task = project.getTasks().create("module", ModuleTask.class);
        final Configuration configuration = project.getConfigurations().maybeCreate("module");
        project.afterEvaluate(p -> {
            Jar jar;
            try {
                jar = (Jar) p.getTasks().getByName("shadowJar");
            } catch (UnknownTaskException ex) {
                jar = (Jar) p.getTasks().getByName("jar");
            }
            jar.finalizedBy(task);

            configuration.getDependencies().ad
        });
    }
}
