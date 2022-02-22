package io.fairyproject.gradle;

import io.fairyproject.gradle.aspect.AjcAction;
import io.fairyproject.gradle.aspect.AspectjCompile;
import io.fairyproject.gradle.aspect.DefaultWeavingSourceSet;
import io.fairyproject.gradle.aspect.WeavingSourceSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;

public class AspectJPlugin implements Plugin<Project> {

    private static final String ASPECTJ_WEAVER = "1.9.7";

    private Configuration aspectJConfiguration;
    private SourceSetContainer sourceSets;
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        this.configureAspectJ();
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
            dependencies.add(project.getDependencies().create("org.aspectj:aspectjtools:" + ASPECTJ_WEAVER));
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
                p.getDependencies().add(sourceSet.getCompileOnlyConfigurationName(), "org.aspectj:aspectjrt:" + ASPECTJ_WEAVER)
        );

        DefaultWeavingSourceSet weavingSourceSet = new DefaultWeavingSourceSet(sourceSet);
        new DslObject(sourceSet).getConvention().add("fairy", weavingSourceSet);

        Configuration aspectpath = project.getConfigurations().create(weavingSourceSet.getAspectConfigurationName());
        weavingSourceSet.setAspectPath(aspectpath);

        Configuration inpath = project.getConfigurations().create(weavingSourceSet.getInpathConfigurationName());
        weavingSourceSet.setInPath(inpath);

        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);
    }
}
