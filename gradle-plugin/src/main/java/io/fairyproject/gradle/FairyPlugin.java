package io.fairyproject.gradle;

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaBasePlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FairyPlugin implements Plugin<Project> {

    public static final Gson GSON = new Gson();
    public static final String REPOSITORY = "https://maven.imanity.dev/repository/imanity-libraries/";
    public static final String DEPENDENCY_FORMAT = "io.fairyproject:%s:%s";
    public static Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();
    public static FairyPlugin INSTANCE;

    public static boolean IS_IN_IDE = false;

    private FairyExtension extension;
    @Getter
    private Project project;

    public void checkIdeIdentityState() {
        IS_IN_IDE = extension.getFairyIde().getOrElse(false);
        if (IS_IN_IDE) {
            IDEDependencyLookup.init(project.getRootProject());
        }
    }

    @Override
    public void apply(@NotNull Project project) {
        INSTANCE = this;
        this.extension = project.getExtensions().create("fairy", FairyExtension.class);
        this.project = project;

        project.getPlugins().apply(JavaBasePlugin.class);
        project.getPlugins().apply(AspectJPlugin.class);
        project.getPlugins().apply(ShadowPlugin.class);

        project.getConfigurations().all(c -> c.resolutionStrategy(resolutionStrategy -> resolutionStrategy.cacheDynamicVersionsFor(30, TimeUnit.SECONDS)));

        final Configuration fairyConfiguration = project.getConfigurations().maybeCreate("fairy");
        final FairyBuildTask fairyTask = project.getTasks().create("fairyBuild", FairyBuildTask.class);

        project.afterEvaluate(p -> {
            long start = System.currentTimeMillis();
            this.checkIdeIdentityState();
            Runnable runnable;
            while ((runnable = QUEUE.poll()) != null) {
                runnable.run();
            }

            if (!IS_IN_IDE) {
                if (this.extension.getLocalRepo().get()) {
                    p.getRepositories().add(p.getRepositories().mavenLocal());
                } else {
                    p.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(REPOSITORY));
                }
            }
            final List<PlatformType> platformTypes = this.extension.getFairyPlatforms().getOrNull();
            if (platformTypes == null) {
                throw new IllegalArgumentException("No platforms found!");
            }

            for (PlatformType platformType : platformTypes) {
                try {
                    platformType.applyDependencies(p, extension);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }

                if (IS_IN_IDE) {
                    final Project bootstrapProject = p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-bootstrap"));

                    ModuleDependency dependency = (ModuleDependency) p.getDependencies().create(bootstrapProject);
                    dependency.setTargetConfiguration("shadow");

                    fairyConfiguration.getDependencies().add(dependency);

                    dependency = (ModuleDependency) p.getDependencies().create(p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-platform")));
                    dependency.setTargetConfiguration("shadow");
                    fairyConfiguration.getDependencies().add(dependency);
                    p.getDependencies().add("aspect", dependency);

                    dependency = (ModuleDependency) p.getDependencies().create(p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-tests")));
                    dependency.setTargetConfiguration("shadow");
                    p.getDependencies().add("testImplementation", dependency);
                } else {
                    final Dependency bootstrapDependency = p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-bootstrap",
                            this.extension.getFairyVersion().get()
                    ));
                    final Dependency platformDependency = p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-platform",
                            this.extension.getFairyVersion().get()
                    ));
                    fairyConfiguration.getDependencies().add(bootstrapDependency);

                    fairyConfiguration.getDependencies().add(platformDependency);
                    p.getDependencies().add("aspect", platformDependency);
                    p.getDependencies().add("testImplementation", String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-tests",
                            this.extension.getFairyVersion().get()
                    ));
                }
            }

            this.extension.getFairyModules().forEach(module -> {
                final Dependency dependency;
                if (IS_IN_IDE) {
                    final Project dependProject = this.project.project(IDEDependencyLookup.getIdentityPath(module));
                    dependency = this.project.getDependencies().create(dependProject);
                    ((ModuleDependency) dependency).setTargetConfiguration("shadow");
                } else {
                    dependency = p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            module,
                            this.extension.getFairyVersion().get()
                    ));
                }

                fairyConfiguration.getDependencies().add(dependency);
            });

            ShadowJar jar = p.getTasks()
                    .withType(ShadowJar.class)
                    .getByName("shadowJar");
            jar.finalizedBy(fairyTask);

            if (platformTypes.contains(PlatformType.APP)) {
                if (!this.extension.getLibraryMode().get()) {
                    jar.getManifest().getAttributes().put("Main-Class", extension.getMainPackage().get() + ".fairy.bootstrap.app.AppLauncher");
                }
                jar.getManifest().getAttributes().put("Multi-Release", "true");
            }

            List<String> implementationModules = new ArrayList<>();
            Multimap<String, String> exclusives = HashMultimap.create();
            for (File file : fairyConfiguration) {
                try (JarFile jarFile = new JarFile(file)) {
                    final ZipEntry entry = jarFile.getEntry("module.json");
                    if (entry != null) {
                        final JsonObject jsonObject = FairyPlugin.GSON.fromJson(new InputStreamReader(jarFile.getInputStream(entry)), JsonObject.class);
                        final String name = jsonObject.get("name").getAsString();

                        implementationModules.add(name);

                        if (jsonObject.has("exclusives")) {
                            for (Map.Entry<String, JsonElement> elementEntry : jsonObject.getAsJsonObject("exclusives").entrySet()) {
                                exclusives.put(elementEntry.getKey(), elementEntry.getValue().getAsString());
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            jar.relocate("io.fairyproject", extension.getMainPackage().get() + ".fairy");

            fairyTask.setInJar(fairyTask.getInJar() != null ? fairyTask.getInJar() : jar.getArchiveFile().get().getAsFile());
            fairyTask.setClassifier(extension.getClassifier().getOrNull());
            fairyTask.setExtension(FairyBuildData.create(extension));
            fairyTask.setExclusions(exclusives);
            fairyTask.setDependModules(implementationModules);
        });
    }

    private static JsonObject readModuleData(Path path) throws IOException {
        final JarFile jarFile = new JarFile(path.toFile());
        final ZipEntry zipEntry = jarFile.getEntry("fairy.json");
        if (zipEntry == null) {
            return null;
        }

        return GSON.fromJson(new InputStreamReader(jarFile.getInputStream(zipEntry)), JsonObject.class);
    }
}
