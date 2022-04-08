package io.fairyproject.gradle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.relocator.Relocation;
import io.fairyproject.shared.FairyVersion;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
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

        project.getConfigurations().all(c -> c.resolutionStrategy(resolutionStrategy -> resolutionStrategy.cacheDynamicVersionsFor(30, TimeUnit.SECONDS)));

        final Configuration fairyConfiguration = project.getConfigurations().maybeCreate("fairy");
        final Configuration downloaderConfiguration = fairyConfiguration.copy();
        final FairyTask fairyTask = project.getTasks().create("fairyBuild", FairyTask.class);
        final FairyTestTask fairyTestTask = project.getTasks().create("fairyTest", FairyTestTask.class);
        project.afterEvaluate(p -> {
            this.checkIdeIdentityState();
            Runnable runnable;
            while ((runnable = QUEUE.poll()) != null) {
                runnable.run();
            }

            p.getConfigurations().getByName("compileClasspath").extendsFrom(fairyConfiguration);

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
                    p.getDependencies().add("testImplementation", dependency);

                    dependency = (ModuleDependency) p.getDependencies().create(p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-platform")));
                    dependency.setTargetConfiguration("shadow");
                    fairyConfiguration.getDependencies().add(dependency);

                    p.getDependencies().add("aspect", dependency);
                    p.getDependencies().add("testImplementation", dependency);

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
                    p.getDependencies().add("testImplementation", bootstrapDependency);

                    fairyConfiguration.getDependencies().add(platformDependency);
                    p.getDependencies().add("aspect", platformDependency);
                    p.getDependencies().add("testImplementation", platformDependency);
                    p.getDependencies().add("testImplementation", String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-tests",
                            this.extension.getFairyVersion().get()
                    ));
                }
            }

            ModuleDependencyTreeLoader dependencyTreeLoader = ModuleDependencyTreeLoader.builder()
                    .project(project)
                    .configuration(fairyConfiguration)
                    .downloaderConfiguration(downloaderConfiguration)
                    .build();
            this.extension.getFairyModules().forEach((key, value) -> {
                final Dependency dependency;
                if (IS_IN_IDE) {
                    final Project dependProject = this.project.project(IDEDependencyLookup.getIdentityPath(key));
                    dependency = this.project.getDependencies().create(dependProject);
                    ((ModuleDependency) dependency).setTargetConfiguration("shadow");
                } else {
                    dependency = p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            key,
                            value
                    ));
                }

                try {
                    dependencyTreeLoader.load(key, dependency);
                } catch (IOException e) {
                    throw new IllegalArgumentException("An error occurs while reading dependency", e);
                }
            });

            Map<String, String> implementationModules = new HashMap<>();
            for (File file : p.getConfigurations().getByName("runtimeClasspath").copy()) {
                final JsonObject jsonObject;
                try {
                    jsonObject = readModuleData(file.toPath());
                } catch (Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                if (jsonObject == null) {
                    continue;
                }

                final JsonArray depends = jsonObject.getAsJsonArray("depends");
                for (JsonElement element : depends) {
                    final String[] split = element.getAsString().split(":");
                    String name = split[0];
                    String version = split[1];
                    // Always get the latest version if multiple dependencies request same module
                    implementationModules.compute(name, (ignored, curVersion) -> {
                        if (curVersion == null) {
                            return version;
                        }
                        final FairyVersion fairyVersion = FairyVersion.parse(curVersion);
                        if (fairyVersion.isAbove(FairyVersion.parse(version))) {
                            return curVersion;
                        } else {
                            return version;
                        }
                    });
                }
            }

            Jar jar;
            try {
                jar = (Jar) p.getTasks().getByName("shadowJar");
            } catch (UnknownTaskException ex) {
                jar = (Jar) p.getTasks().getByName("jar");
            }
            jar.finalizedBy(fairyTask);

            p.getTasks().getByName("compileTestJava").finalizedBy(fairyTestTask);

            if (platformTypes.contains(PlatformType.APP)) {
                if (!this.extension.getLibraryMode().get()) {
                    jar.getManifest().getAttributes().put("Main-Class", extension.getMainPackage().get() + ".fairy.bootstrap.app.AppLauncher");
                }
                jar.getManifest().getAttributes().put("Multi-Release", "true");
            }

            List<Object> list = new ArrayList<>();
            for (File file : fairyConfiguration) {
                list.add(file.isDirectory() ? file : project.zipTree(file));
            }
            jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

            jar.from(list);

            List<Relocation> relocations = new ArrayList<>();
            if (!this.extension.getLibraryMode().get()) {
                relocations.add(new Relocation("io.fairyproject", extension.getMainPackage().get() + ".fairy").setOnlyRelocateShaded(true));
            }

            fairyTask.setInJar(fairyTask.getInJar() != null ? fairyTask.getInJar() : jar.getArchiveFile().get().getAsFile());
            fairyTask.setClassifier(extension.getClassifier().getOrNull());
            fairyTask.setRelocations(relocations);
            fairyTask.setExtension(extension);
            fairyTask.setExclusions(dependencyTreeLoader.getExclusives());
            fairyTask.setDependModules(implementationModules);
            fairyTestTask.setExtension(extension);
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
