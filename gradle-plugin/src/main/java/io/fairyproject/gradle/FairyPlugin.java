package io.fairyproject.gradle;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.aspectj.AjcAction;
import io.fairyproject.gradle.aspectj.AspectjCompile;
import io.fairyproject.gradle.aspectj.DefaultWeavingSourceSet;
import io.fairyproject.gradle.aspectj.WeavingSourceSet;
import io.fairyproject.gradle.relocator.Relocation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FairyPlugin implements Plugin<Project> {

    private static final String REPOSITORY = "https://maven.imanity.dev/repository/imanity-libraries/";
    private static final String DEPENDENCY_FORMAT = "io.fairyproject:framework:%s:%s";

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
            p.getConfigurations().getByName("compileClasspath").extendsFrom(fairyConfiguration);
            p.getConfigurations().getByName("compileClasspath").extendsFrom(fairyModuleConfiguration);
            p.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(REPOSITORY));
            final List<PlatformType> platformTypes = this.extension.getFairyPlatforms().getOrNull();
            if (platformTypes == null) {
                throw new IllegalArgumentException("No platforms found!");
            }

            for (PlatformType platformType : platformTypes) {
                fairyConfiguration.getDependencies().add(p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                        this.extension.getFairyVersion().get(),
                        platformType.getDependencyName() + "-bootstrap"
                )));
                p.getDependencies().add("compileOnly", String.format(DEPENDENCY_FORMAT,
                        this.extension.getFairyVersion().get(),
                        platformType.getDependencyName() + "-platform"
                ));
            }

            final Configuration copy = fairyModuleConfiguration.copy();
            for (String moduleName : this.extension.getFairyModules().get()) {
                final Dependency dependency = p.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                        this.extension.getFairyVersion().get(),
                        moduleName
                ));

                try {
                    new ModuleReader(project, fairyModuleConfiguration, copy, new HashSet<>()).load(moduleName, dependency, new ArrayList<>());
                } catch (IOException e) {
                    throw new IllegalArgumentException("An error occurs while reading dependency");
                }
            }

            Jar jar = (Jar) p.getTasks().getByName("jar");
            jar.finalizedBy(fairyTask);

            if (platformTypes.contains(PlatformType.APP)) {
                jar.getManifest().getAttributes().put("Main-Class", extension.getMainPackage().get() + ".fairy.bootstrap.app.Main");
            }

            List<Object> list = new ArrayList<>();
            for (File file : fairyConfiguration) {
                list.add(file.isDirectory() ? file : project.zipTree(file));
            }

            jar.from(list);

            List<Relocation> relocations = new ArrayList<>();
            relocations.add(new Relocation("io.fairyproject", extension.getMainPackage().get() + ".fairy").setOnlyRelocateShaded(true));

            fairyTask.setInJar(fairyTask.getInJar() != null ? fairyTask.getInJar() : jar.getArchiveFile().get().getAsFile());
            fairyTask.setClassifier(extension.getClassifier().getOrNull());
            fairyTask.setRelocations(relocations);
            fairyTask.setExtension(extension);
        });
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

                    for (Pair<String, Dependency> pair : this.readDependencies(jsonObject)) {
                        final List<String> tree = new ArrayList<>(moduleTree);
                        this.load(pair.getKey(), pair.getValue(), tree);
                    }
                }
            }
        }

        private List<Pair<String, Dependency>> readDependencies(JsonObject jsonObject) {
            List<Pair<String, Dependency>> list = new ArrayList<>();
            if (jsonObject.has("depends")) {
                for (JsonElement element : jsonObject.getAsJsonArray("depends")) {
                    JsonObject dependJson = element.getAsJsonObject();
                    final String name = dependJson.get("module").getAsString();
                    list.add(Pair.of(name, this.project.getDependencies().create(String.format(DEPENDENCY_FORMAT,
                            extension.getFairyVersion().get(),
                            name
                    ))));
                }
            }
            return list;
        }

    }
}
