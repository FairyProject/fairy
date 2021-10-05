package org.fairy.gradle;

import org.fairy.gradle.aspectj.AspectjCompile;
import org.fairy.gradle.aspectj.DefaultWeavingSourceSet;
import org.fairy.gradle.aspectj.WeavingSourceSet;
import org.fairy.gradle.relocator.Relocation;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
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
import org.fairy.gradle.aspectj.AjcAction;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FairyPlugin implements Plugin<Project> {

    private static final String REPOSITORY = "https://maven.imanity.dev/repository/imanity-libraries/";
    private static final String BOOTSTRAP_DEPENDENCY_FORMAT = "org.fairy:%s-bootstrap:%s";
    private static final String MAIN_DEPENDENCY_FORMAT = "org.fairy:%s-main:%s";

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
        final FairyTask fairyTask = project.getTasks().create("fairyBuild", FairyTask.class);
        project.afterEvaluate(p -> {
            p.getConfigurations().getByName("compileClasspath").extendsFrom(fairyConfiguration);
            p.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(REPOSITORY));
            final List<PlatformType> platformTypes = this.extension.getFairyPlatforms().getOrNull();
            if (platformTypes == null) {
                throw new IllegalArgumentException("No platforms found!");
            }

            for (PlatformType platformType : platformTypes) {
                fairyConfiguration.getDependencies().add(p.getDependencies().create(String.format(BOOTSTRAP_DEPENDENCY_FORMAT,
                        platformType.getDependencyName(),
                        this.extension.getFairyVersion().get()
                )));
                p.getDependencies().add("compileOnly", String.format(MAIN_DEPENDENCY_FORMAT,
                        platformType.getDependencyName(),
                        this.extension.getFairyVersion().get()
                ));
            }

            Jar jar = (Jar) p.getTasks().getByName("jar");
            jar.finalizedBy(fairyTask);

            List<Object> list = new ArrayList<>();
            for (File file : fairyConfiguration) {
                list.add(file.isDirectory() ? file : project.zipTree(file));
            }

            jar.from(list);

            List<Relocation> relocations = new ArrayList<>();
            relocations.add(new Relocation("org.fairy", extension.getShadedPackage().get() + ".fairy").setOnlyRelocateShaded(true));

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
}
