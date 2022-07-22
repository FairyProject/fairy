package io.fairyproject.gradle;

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.util.MavenUtil;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaBasePlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FairyPlugin implements Plugin<Project> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String REPOSITORY = "https://repo.imanity.dev/imanity-libraries/";
    public static final String DEPENDENCY_FORMAT = "io.fairyproject:%s:%s";
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
        project.getPlugins().apply(ShadowPlugin.class);

        project.getConfigurations().all(c -> c.resolutionStrategy(resolutionStrategy -> resolutionStrategy.cacheDynamicVersionsFor(30, TimeUnit.SECONDS)));

        final Configuration fairyConfiguration = project.getConfigurations().maybeCreate("fairy");
        final FairyBuildTask fairyTask = project.getTasks().create("fairyBuild", FairyBuildTask.class);

        project.afterEvaluate(p -> this.applyInternal(p, fairyConfiguration, fairyTask));
    }

    private void applyInternal(Project project, Configuration fairyConfiguration, FairyBuildTask fairyTask) {
        long start = System.currentTimeMillis();

        MavenUtil.start(false, project.getBuildFile());
        try {
            this.checkIdeIdentityState();

            if (!IS_IN_IDE) {
                if (this.extension.getLocalRepo().get()) {
                    this.project.getRepositories().add(this.project.getRepositories().mavenLocal());
                } else {
                    this.project.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(REPOSITORY));
                }
            }
            final List<PlatformType> platformTypes = this.extension.getFairyPlatforms().getOrNull();
            if (platformTypes == null) {
                throw new IllegalArgumentException("No platforms found!");
            }

            final DependencySet dependencies = fairyConfiguration.getDependencies();
            for (PlatformType platformType : platformTypes) {
                this.applyPlatform(project, dependencies, platformType);
            }

            this.extension.getFairyModules().forEach(module -> this.applyModule(project, dependencies, module));

            ShadowJar jar = project.getTasks()
                    .withType(ShadowJar.class)
                    .getByName("shadowJar");
            jar.finalizedBy(fairyTask);

            if (platformTypes.contains(PlatformType.APP)) {
                if (!this.extension.getLibraryMode().get()) {
                    jar.getManifest().getAttributes().put("Main-Class", extension.getMainPackage().get() + ".fairy.bootstrap.app.AppLauncher");
                }
                jar.getManifest().getAttributes().put("Multi-Release", "true");
            }

            Set<Lib> libraries = new HashSet<>();
            List<String> modules = new ArrayList<>();
            Multimap<String, String> exclusives = HashMultimap.create();
            for (File file : fairyConfiguration) {
                this.readFile(libraries, modules, exclusives, file);
            }

            jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            jar.relocate("io.fairyproject", extension.getMainPackage().get() + ".fairy");

            for (Dependency dependency : dependencies) {
                project.getDependencies().add("implementation", dependency);
                project.getDependencies().add("testImplementation", dependency);
            }

            libraries.addAll(extension.getLibraries());
            for (Lib library : libraries) {
                if (library.getRepository() != null) {
                    project.getRepositories().add(project.getRepositories().maven(repo -> repo.setUrl(library.getRepository())));
                }
                project.getDependencies().add("compileOnly", library.getDependency());
                project.getDependencies().add("testImplementation", library.getDependency());
            }

            fairyTask.setInJar(fairyTask.getInJar() != null ? fairyTask.getInJar() : jar.getArchiveFile().get().getAsFile());
            fairyTask.setClassifier(extension.getClassifier().getOrNull());
            fairyTask.setExtension(FairyBuildData.create(extension, libraries));
            fairyTask.setExclusions(exclusives);
            fairyTask.setDependModules(modules);
        } finally {
            MavenUtil.end();
            System.out.println("Initialized FairyPlugin within " + (System.currentTimeMillis() - start) + "ms.");
        }
    }

    private void readFile(Set<Lib> libraries, List<String> modules, Multimap<String, String> exclusives, File file) {
        try (JarFile jarFile = new JarFile(file)) {
            final ZipEntry entry = jarFile.getEntry("module.json");
            if (entry != null) {
                final JsonObject jsonObject = FairyPlugin.GSON.fromJson(new InputStreamReader(jarFile.getInputStream(entry)), JsonObject.class);
                final String name = jsonObject.get("name").getAsString();

                modules.add(name);

                if (jsonObject.has("exclusives")) {
                    for (Map.Entry<String, JsonElement> elementEntry : jsonObject.getAsJsonObject("exclusives").entrySet()) {
                        exclusives.put(elementEntry.getKey(), elementEntry.getValue().getAsString());
                    }
                }

                if (jsonObject.has("libraries")) {
                    for (JsonElement element : jsonObject.getAsJsonArray("libraries")) {
                        final JsonObject library = element.getAsJsonObject();
                        libraries.add(Lib.fromJsonObject(library));
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void applyModule(Project project, DependencySet dependencies, String module) {
        final Dependency dependency;
        if (IS_IN_IDE) {
            final Project dependProject = this.project.project(IDEDependencyLookup.getIdentityPath(module));
            dependency = this.project.getDependencies().create(dependProject);
            ((ModuleDependency) dependency).setTargetConfiguration("shadow");
        } else {
            dependency = project.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                    module,
                    this.extension.getFairyVersion().get()
            ));
        }

        dependencies.add(dependency);
    }

    private void applyPlatform(Project project, DependencySet dependencies, PlatformType platformType) {
        try {
            platformType.applyDependencies(project, extension);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        if (IS_IN_IDE) {
            final Project bootstrapProject = project.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-bootstrap"));

            ModuleDependency dependency = (ModuleDependency) project.getDependencies().create(bootstrapProject);
            dependency.setTargetConfiguration("shadow");

            dependencies.add(dependency);

            dependency = (ModuleDependency) project.getDependencies().create(project.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-platform")));
            dependency.setTargetConfiguration("shadow");
            dependencies.add(dependency);

            dependency = (ModuleDependency) project.getDependencies().create(project.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-tests")));
            dependency.setTargetConfiguration("shadow");
            project.getDependencies().add("testImplementation", dependency);
        } else {
            final Dependency bootstrapDependency = project.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                    platformType.getDependencyName() + "-bootstrap",
                    this.extension.getFairyVersion().get()
            ));
            final Dependency platformDependency = project.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                    platformType.getDependencyName() + "-platform",
                    this.extension.getFairyVersion().get()
            ));
            dependencies.add(bootstrapDependency);
            dependencies.add(platformDependency);
            project.getDependencies().add("testImplementation", String.format(DEPENDENCY_FORMAT,
                    platformType.getDependencyName() + "-tests",
                    this.extension.getFairyVersion().get()
            ));
        }
    }
}
