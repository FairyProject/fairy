package io.fairyproject.gradle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.aspectj.AjcAction;
import io.fairyproject.gradle.aspectj.AspectjCompile;
import io.fairyproject.gradle.aspectj.DefaultWeavingSourceSet;
import io.fairyproject.gradle.aspectj.WeavingSourceSet;
import io.fairyproject.gradle.relocator.Relocation;
import io.fairyproject.gradle.util.FairyVersion;
import io.fairyproject.gradle.util.MavenUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.plugins.*;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FairyPlugin implements Plugin<Project> {

    private static final String REPOSITORY = "https://maven.imanity.dev/repository/imanity-libraries/";
    private static final String DEPENDENCY_FORMAT = "io.fairyproject:%s:%s";
    public static boolean IS_IN_IDE = false;

    private FairyExtension extension;
    private Configuration aspectJConfiguration;
    private SourceSetContainer sourceSets;
    private Project project;

    @Override
    public void apply(@NotNull Project project) {
        this.extension = project.getExtensions().create("fairy", FairyExtension.class);
        this.project = project;

        project.getPlugins().apply(JavaBasePlugin.class);

        this.configureAspectJ();

        final Configuration fairyConfiguration = project.getConfigurations().maybeCreate("fairy");
        final Configuration fairyModuleConfiguration = project.getConfigurations().maybeCreate("fairyModule");
        final FairyTask fairyTask = project.getTasks().create("fairyBuild", FairyTask.class);
        project.afterEvaluate(p -> {
            IS_IN_IDE = extension.getFairyIde().getOrElse(false);
            if (IS_IN_IDE) {
                IDEDependencyLookup.init(project.getRootProject());
            }

            p.getConfigurations().getByName("compileClasspath").extendsFrom(fairyConfiguration);
            p.getConfigurations().getByName("compileClasspath").extendsFrom(fairyModuleConfiguration);
            if (!IS_IN_IDE)
                p.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(REPOSITORY));
            final List<PlatformType> platformTypes = this.extension.getFairyPlatforms().getOrNull();
            if (platformTypes == null) {
                throw new IllegalArgumentException("No platforms found!");
            }

            for (PlatformType platformType : platformTypes) {
                if (IS_IN_IDE) {
                    final Project bootstrapProject = p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-bootstrap"));

                    ModuleDependency dependency = (ModuleDependency) p.getDependencies().create(bootstrapProject);
                    dependency.setTargetConfiguration("shadow");

                    fairyConfiguration.getDependencies().add(dependency);

                    dependency = (ModuleDependency) p.getDependencies().create(p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-platform")));
                    dependency.setTargetConfiguration("shadow");
                    p.getDependencies().add("compileOnly", dependency);
                    p.getDependencies().add("testImplementation", dependency);

                    dependency = (ModuleDependency) p.getDependencies().create(p.project(IDEDependencyLookup.getIdentityPath(platformType.getDependencyName() + "-tests")));
                    dependency.setTargetConfiguration("shadow");
                    p.getDependencies().add("testImplementation", dependency);
                } else {
                    fairyConfiguration.getDependencies().add(p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-bootstrap",
                            this.extension.getFairyVersion().get()
                    )));
                    p.getDependencies().add("compileOnly", String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-platform",
                            this.extension.getFairyVersion().get()
                    ));
                    p.getDependencies().add("testImplementation", String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-platform",
                            this.extension.getFairyVersion().get()
                    ));
                    p.getDependencies().add("testImplementation", String.format(DEPENDENCY_FORMAT,
                            platformType.getDependencyName() + "-tests",
                            this.extension.getFairyVersion().get()
                    ));
                }
            }

            final Configuration copy = fairyModuleConfiguration.copy();
            for (Map.Entry<String, String> moduleEntry : this.extension.getFairyModules().entrySet()) {
                final Dependency dependency;
                if (IS_IN_IDE) {
                    final Project dependProject = this.project.project(IDEDependencyLookup.getIdentityPath(moduleEntry.getKey()));
                    dependency = this.project.getDependencies().create(dependProject);
                    ((ModuleDependency) dependency).setTargetConfiguration("shadow");
                } else {
                    dependency = p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            moduleEntry.getKey(),
                            moduleEntry.getValue()
                    ));
                }

                try {
                    new ModuleReader(project, extension, fairyModuleConfiguration, copy, new HashSet<>()).load(moduleEntry.getKey(), dependency, new ArrayList<>());
                } catch (IOException e) {
                    throw new IllegalArgumentException("An error occurs while reading dependency");
                }
            }

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

            jar.from(list);

            List<Relocation> relocations = new ArrayList<>();
            if (!this.extension.getLibraryMode().get()) {
                relocations.add(new Relocation("io.fairyproject", extension.getMainPackage().get() + ".fairy").setOnlyRelocateShaded(true));
            }

            fairyTask.setInJar(fairyTask.getInJar() != null ? fairyTask.getInJar() : jar.getArchiveFile().get().getAsFile());
            fairyTask.setRelocateEntries(fairyModuleConfiguration.getFiles());
            fairyTask.setClassifier(extension.getClassifier().getOrNull());
            fairyTask.setRelocations(relocations);
            fairyTask.setExtension(extension);
            fairyTask.setDependModules(implementationModules);
        });
    }

    private static JsonObject readModuleData(Path path) throws IOException {
        final JarFile jarFile = new JarFile(path.toFile());
        final ZipEntry zipEntry = jarFile.getEntry("fairy.json");
        if (zipEntry == null) {
            return null;
        }

        return new Gson().fromJson(new InputStreamReader(jarFile.getInputStream(zipEntry)), JsonObject.class);
    }

    private void configurePlugin(String language) {
        sourceSets.all(sourceSet -> {
            WeavingSourceSet weavingSourceSet = new DslObject(sourceSet).getConvention().getByType(WeavingSourceSet.class);

            FileCollection aspectPath = weavingSourceSet.getAspectPath();
            FileCollection inPath = weavingSourceSet.getInPath();

            project.getTasks().named(sourceSet.getCompileTaskName(language), AbstractCompile.class, compileTask -> {
                AjcAction ajcAction = enhanceWithWeavingAction(compileTask, aspectPath, inPath, this.aspectJConfiguration);
                // add main source set if it's test
                if (compileTask.getName().contains(SourceSet.TEST_SOURCE_SET_NAME)) {
                    ajcAction.getOptions().getAspectpath().from(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME));
                }
                if (compileTask instanceof HasCompileOptions) {
                    HasCompileOptions compileTaskWithOptions = (HasCompileOptions) compileTask;
                    ajcAction.getOptions().getBootclasspath().from(compileTaskWithOptions.getOptions().getBootstrapClasspath());
                    ajcAction.getOptions().getExtdirs().from(compileTaskWithOptions.getOptions().getExtensionDirs());
                }
            });
        });
    }

    private AjcAction enhanceWithWeavingAction(AbstractCompile abstractCompile, FileCollection aspectpath, FileCollection inpath, Configuration aspectjConfiguration) {
        AjcAction action = project.getObjects().newInstance(AjcAction.class);

        action.getOptions().getAspectpath().from(aspectpath);
        action.getOptions().getInpath().from(inpath);
        action.getAdditionalInpath().from(abstractCompile.getDestinationDirectory());
        action.getClasspath().from(aspectjConfiguration);

        action.getOptions().getCompilerArgs().add("-showWeaveInfo");
        action.getOptions().getCompilerArgs().add("-verbose");

        action.addToTask(abstractCompile);

        return action;
    }

    private void configureAspectJ() {
        this.aspectJConfiguration = project.getConfigurations().create("aspectj");
        this.aspectJConfiguration.defaultDependencies(dependencies -> {
            dependencies.add(project.getDependencies().create("org.aspectj:aspectjtools:" + extension.getAspectJVersion().get()));
        });

        project.getTasks().withType(AspectjCompile.class).configureEach(aspectjCompile -> {
            aspectjCompile.getAspectjClasspath().from(this.aspectJConfiguration);
        });

        sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();

        sourceSets.all(this::configureSourceSetDefaults);

        project.getPlugins().withType(JavaPlugin.class, plugin -> this.configurePlugin("java"));
        project.getPlugins().withType(GroovyPlugin.class, plugin -> this.configurePlugin("groovy"));
        project.getPlugins().withType(ScalaPlugin.class, plugin -> this.configurePlugin("scala"));
        project.getPlugins().withId("org.jetbrains.kotlin.jvm", plugin -> this.configurePlugin("kotlin"));
    }

    private void configureSourceSetDefaults(SourceSet sourceSet) {
        project.afterEvaluate(p ->
                p.getDependencies().add(sourceSet.getImplementationConfigurationName(), "org.aspectj:aspectjrt:" + this.extension.getAspectJVersion().get())
        );

        DefaultWeavingSourceSet weavingSourceSet = new DefaultWeavingSourceSet(sourceSet);
        new DslObject(sourceSet).getConvention().add("fairy", weavingSourceSet);

        Configuration aspectpath = project.getConfigurations().create(weavingSourceSet.getAspectConfigurationName());
        weavingSourceSet.setAspectPath(aspectpath);

        Configuration inpath = project.getConfigurations().create(weavingSourceSet.getInpathConfigurationName());
        weavingSourceSet.setInPath(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspectpath);
        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);
    }

    @RequiredArgsConstructor
    private class ModuleReader {

        private final Gson gson = new Gson();
        private final Project project;
        private final FairyExtension extension;
        private final Configuration fairyModuleConfiguration;
        private final Configuration copiedConfiguration;
        private final Set<String> allLoadedModules;

        public void load(String moduleName, Dependency dependency, List<String> moduleTree) throws IOException {
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
            fairyModuleConfiguration.getDependencies().add(dependency);

            // copy for retrieve files
            final Configuration copy = copiedConfiguration.copy();
            copy.getDependencies().add(dependency);
            for (File file : copy.files(dependency)) {
                JarFile jarFile = new JarFile(file);
                final ZipEntry entry = jarFile.getEntry("module.json");
                if (entry != null) {
                    final JsonObject jsonObject = gson.fromJson(new InputStreamReader(jarFile.getInputStream(entry)), JsonObject.class);

                    if (jsonObject.has("libraries")) {
                        for (JsonElement element : jsonObject.getAsJsonArray("libraries")) {
                            final JsonObject library = element.getAsJsonObject();
                            final String tag = library.get("dependency").getAsString();
                            if (library.has("repository")) {
                                final String repository = library.get("repository").getAsString();
                                this.project.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(repository));
                            }
                            this.project.getDependencies().add("compileOnly", this.project.getDependencies().create(tag));
                        }
                    }

                    for (Pair<String, Dependency> pair : this.readDependencies(jsonObject)) {
                        final List<String> tree = new ArrayList<>(moduleTree);
                        this.load(pair.getKey(), pair.getValue(), tree);
                    }
                }
            }
        }

        private List<Pair<String, Dependency>> readDependencies(JsonObject jsonObject) throws IOException {
            List<Pair<String, Dependency>> list = new ArrayList<>();
            if (jsonObject.has("depends")) {
                for (JsonElement element : jsonObject.getAsJsonArray("depends")) {
                    final String full = element.getAsString();
                    final String[] split = full.split(":");
                    final String name = split[0];
                    String version = split[1];
                    if (version == null) {
                        version = MavenUtil.getLatest(name);
                    }
                    if (IS_IN_IDE) {
                        final Project dependProject = this.project.project(IDEDependencyLookup.getIdentityPath(name));
                        final ModuleDependency dependency = (ModuleDependency) this.project.getDependencies().create(dependProject);
                        dependency.setTargetConfiguration("shadow");

                        list.add(Pair.of(name, dependency));
                    } else {
                        list.add(Pair.of(name, this.project.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                                name,
                                version
                        ))));
                    }
                }
            }
            return list;
        }

    }
}
