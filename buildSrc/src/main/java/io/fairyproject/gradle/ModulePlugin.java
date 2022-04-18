package io.fairyproject.gradle;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.jvm.tasks.Jar;

import java.util.*;

public class ModulePlugin implements Plugin<Project> {

    private static final Gson GSON = new Gson();
    protected static final String MODULE_PREFIX = ":io.fairyproject.modules:";
    protected static final String PLATFORM_PREFIX = ":io.fairyproject.platforms:";
    protected static final String TEST_PREFIX = ":io.fairyproject.tests:";

    @SneakyThrows
    @Override
    public void apply(Project project) {
        final ModuleExtension extension = project.getExtensions().create("module", ModuleExtension.class);
        final ModuleTask task = project.getTasks().create("module", ModuleTask.class);
        final PublishSnapshotTask publishSnapshotDevTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotDev");
        final PublishSnapshotTask publishSnapshotLocalTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotLocal");
        final PublishSnapshotTask publishSnapshotProductionTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotProduction");

        project.afterEvaluate(i -> {
            publishSnapshotDevTask.setModuleTask(task);
            publishSnapshotLocalTask.setModuleTask(task);
            publishSnapshotProductionTask.setModuleTask(task);

            Map<Lib, Boolean> libs = new HashMap<>(extension.getLibraries().getOrElse(Collections.emptyMap()));
            List<Pair<String, String>> exclusives = new ArrayList<>();
            for (String moduleName : extension.getDepends().get()) {
                final Project module = project.project(MODULE_PREFIX + moduleName);
                project.evaluationDependsOn(MODULE_PREFIX + moduleName);

                Dependency dependency = project.getDependencies().create(module);

                project.getDependencies().add("implementation", dependency);
                project.getDependencies().add("testImplementation", dependency);

                final ModuleExtension moduleExtension = module.getExtensions().getByType(ModuleExtension.class);
                for (Map.Entry<String, String> entry : moduleExtension.getExclusives().get().entrySet()) {
                    exclusives.add(Pair.of(entry.getKey(), entry.getValue()));
                }

                libs.putAll(moduleExtension.getLibraries().get());
            }

            for (String module : extension.getSubDepends().get()) {
                final Dependency dependency = project.getDependencies().create(project.project(MODULE_PREFIX + module));

                project.getDependencies().add("compileOnly", dependency);
                project.getDependencies().add("testImplementation", dependency);
            }

            for (String platform : extension.getPlatforms().get()) {
                final Dependency dependency = project.getDependencies().create(project.project(PLATFORM_PREFIX + platform + "-platform"));
                final Dependency testDependency = project.getDependencies().create(project.project(TEST_PREFIX + platform + "-tests"));

                project.getDependencies().add("compileOnly", dependency);
                project.getDependencies().add("testImplementation", dependency);
                project.getDependencies().add("testImplementation", testDependency);

                if (platform.equals("bukkit")) {
                    project.getDependencies().add("testImplementation", "dev.imanity.mockbukkit:MockBukkit1.16:1.0.17");
                }
            }

            for (Map.Entry<Lib, Boolean> libraryEntry : libs.entrySet()) {
                final Lib library = libraryEntry.getKey();

                if (library.getRepository() != null) {
                    project.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(library.getRepository()));
                }
                final Dependency dependency = project.getDependencies().create(library.getDependency());
                if (libraryEntry.getValue()) {
                    project.getDependencies().add("implementation", dependency);
                } else {
                    project.getDependencies().add("compileOnlyApi", dependency);
                }
                project.getDependencies().add("testImplementation", dependency);
            }

            Jar jar;
            try {
                jar = (Jar) project.getTasks().getByName("shadowJar");
            } catch (UnknownTaskException ex) {
                jar = (Jar) project.getTasks().getByName("jar");
            }
            jar.finalizedBy(task);

            task.setInJar(task.getInJar() != null ? task.getInJar() : jar.getArchiveFile().get().getAsFile());
            task.setExtension(ModuleExtensionSerializable.create(extension, libs));
            task.setExclusives(exclusives);
        });
    }
}
