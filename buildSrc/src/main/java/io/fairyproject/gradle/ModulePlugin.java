package io.fairyproject.gradle;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.jvm.tasks.Jar;

import java.util.*;

public class ModulePlugin implements Plugin<Project> {

    protected static final String MODULE_PREFIX = ":io.fairyproject.modules:";
    protected static final String PLATFORM_PREFIX = ":io.fairyproject.platforms:";

    @Override
    public void apply(Project project) {
        final ModuleExtension extension = project.getExtensions().create("module", ModuleExtension.class);
        final ModuleTask task = project.getTasks().create("module", ModuleTask.class);
        final PublishSnapshotTask publishSnapshotDevTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotDev");
        final PublishSnapshotTask publishSnapshotProductionTask = (PublishSnapshotTask) project.getTasks().getByName("publishSnapshotProduction");

        final Configuration configuration = project.getConfigurations().maybeCreate("module");
        project.afterEvaluate(p -> {
            publishSnapshotDevTask.setModuleTask(task);
            publishSnapshotProductionTask.setModuleTask(task);

            p.getConfigurations().getByName("compileClasspath").extendsFrom(configuration);

            Set<String> loaded = new HashSet<>();
            for (String module : extension.getDepends().get()) {
                new ModuleReader(p, configuration, loaded).load(module, p.project(MODULE_PREFIX + module), new ArrayList<>());
            }
            Set<String> actualDepends = ImmutableSet.copyOf(loaded);

            for (String module : extension.getSubDepends().get()) {
                new ModuleReader(p, configuration, loaded).load(module, p.project(MODULE_PREFIX + module), new ArrayList<>());
            }

            for (String platform : extension.getPlatforms().get()) {
                configuration.getDependencies().add(p.getDependencies().create(p.project(PLATFORM_PREFIX + platform + "-platform")));
            }

            for (Lib library : extension.getLibraries().getOrElse(Collections.emptyList())) {
                if (library.getRepository() != null) {
                    p.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(library.getRepository()));
                }
                configuration.getDependencies().add(project.getDependencies().create(library.getDependency()));
            }

            Jar jar;
            try {
                jar = (Jar) p.getTasks().getByName("shadowJar");
            } catch (UnknownTaskException ex) {
                jar = (Jar) p.getTasks().getByName("jar");
            }
            jar.finalizedBy(task);

            task.setInJar(task.getInJar() != null ? task.getInJar() : jar.getArchiveFile().get().getAsFile());
            task.setExtension(extension);
            task.setModules(actualDepends);
        });
    }

    @RequiredArgsConstructor
    private static class ModuleReader {

        private final Project project;
        private final Configuration fairyModuleConfiguration;
        private final Set<String> allLoadedModules;

        public void load(String moduleName, Object dependency, List<String> moduleTree) {
            if (moduleTree.contains(moduleName)) {
                moduleTree.add(moduleName);

                StringBuilder stringBuilder = new StringBuilder();
                final Iterator<String> iterator = moduleTree.iterator();

                while (iterator.hasNext()) {
                    final String name = iterator.next();
                    if (name.equals(moduleName)) {
                        stringBuilder.append("*").append(name).append("*");
                    } else {
                        stringBuilder.append(name);
                    }
                    if (iterator.hasNext()) {
                        stringBuilder.append(" -> ");
                    }
                }
                throw new IllegalStateException("Circular dependency: " + stringBuilder);
            }

            moduleTree.add(moduleName);
            if (allLoadedModules.contains(moduleName)) {
                return;
            }

            allLoadedModules.add(moduleName);
            fairyModuleConfiguration.getDependencies().add(project.getDependencies().create(dependency));

            // copy for retrieve files
            for (Pair<String, Object> pair : this.readDependencies(this.project.project(MODULE_PREFIX + moduleName))) {
                final List<String> tree = new ArrayList<>(moduleTree);
                this.load(pair.getKey(), pair.getValue(), tree);
            }
        }

        private List<Pair<String, Object>> readDependencies(Project project) {
            List<Pair<String, Object>> list = new ArrayList<>();
            final ModuleExtension extension = project.getExtensions().findByType(ModuleExtension.class);
            if (extension != null) {
                for (Lib library : extension.getLibraries().getOrElse(Collections.emptyList())) {
                    if (library.getRepository() != null) {
                        this.project.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(library.getRepository()));
                    }
                    fairyModuleConfiguration.getDependencies().add(project.getDependencies().create(library.getDependency()));
                }
                for (String depend : extension.getDepends().get()) {
                    list.add(Pair.of(depend, this.project.project(MODULE_PREFIX + depend)));
                }
            }
            return list;
        }

    }
}
