package io.fairyproject.gradle;

import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.jvm.tasks.Jar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModulePlugin implements Plugin<Project> {

    private static final Gson GSON = new Gson();
    protected static final String MODULE_PREFIX = ":io.fairyproject.modules:";
    protected static final String PLATFORM_PREFIX = ":io.fairyproject.platforms:";

    @Override
    public void apply(Project project) {
        final ModuleExtension extension = project.getExtensions().create("module", ModuleExtension.class);
        final ModuleTask task = project.getTasks().create("module", ModuleTask.class);
        final PublishSnapshotTask publishSnapshotDevTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotDev");
        final PublishSnapshotTask publishSnapshotLocalTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotLocal");
        final PublishSnapshotTask publishSnapshotProductionTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotProduction");

        project.afterEvaluate(p -> {
            publishSnapshotDevTask.setModuleTask(task);
            publishSnapshotLocalTask.setModuleTask(task);
            publishSnapshotProductionTask.setModuleTask(task);

            List<Pair<String, String>> exclusives = new ArrayList<>();
            for (String module : extension.getDepends().get()) {
                final Project moduleProject = p.project(MODULE_PREFIX + module);
                Dependency dependency = p.getDependencies().create(p.project(MODULE_PREFIX + module));

                p.getDependencies().add("implementation", dependency);
                p.getDependencies().add("testImplementation", dependency);

                final ModuleExtension moduleExtension = moduleProject.getExtensions().getByType(ModuleExtension.class);
                for (Map.Entry<String, String> entry : moduleExtension.getExclusives().get().entrySet()) {
                    exclusives.add(Pair.of(entry.getKey(), entry.getValue()));
                }
            }

            for (String module : extension.getSubDepends().get()) {
                final Dependency dependency = p.getDependencies().create(p.project(MODULE_PREFIX + module));

                p.getDependencies().add("compileOnly", dependency);
                p.getDependencies().add("testImplementation", dependency);
            }

            for (String platform : extension.getPlatforms().get()) {
                final Dependency dependency = p.getDependencies().create(p.project(PLATFORM_PREFIX + platform + "-platform"));

                p.getDependencies().add("compileOnly", dependency);
                p.getDependencies().add("testImplementation", dependency);
            }

            for (Map.Entry<Lib, Boolean> libraryEntry : extension.getLibraries().getOrElse(Collections.emptyMap()).entrySet()) {
                final Lib library = libraryEntry.getKey();
                if (library.getRepository() != null) {
                    p.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(library.getRepository()));
                }
                final Dependency dependency = project.getDependencies().create(library.getDependency());
                if (libraryEntry.getValue()) {
                    p.getDependencies().add("implementation", dependency);
                } else {
                    p.getDependencies().add("compileOnlyApi", dependency);
                }
                p.getDependencies().add("testImplementation", dependency);
            }

            Jar jar;
            try {
                jar = (Jar) p.getTasks().getByName("shadowJar");
            } catch (UnknownTaskException ex) {
                jar = (Jar) p.getTasks().getByName("jar");
            }
            jar.finalizedBy(task);

            task.setInJar(task.getInJar() != null ? task.getInJar() : jar.getArchiveFile().get().getAsFile());
            task.setExtension(ModuleExtensionSerializable.create(extension));
            task.setExclusives(exclusives);
        });
    }
}
