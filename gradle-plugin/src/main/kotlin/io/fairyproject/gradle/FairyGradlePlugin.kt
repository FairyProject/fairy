package io.fairyproject.gradle

import io.fairyproject.gradle.compiler.FairyCompilerAction
import io.fairyproject.gradle.constants.UrlConstants
import io.fairyproject.gradle.dependency.DependencyManagementPluginAction
import io.fairyproject.gradle.extension.FairyExtension
import io.fairyproject.gradle.resource.FairyResourcePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.SourceSetContainer

/**
 * Fairy Gradle plugin.
 *
 * @since 0.6.4b1
 */
class FairyGradlePlugin : Plugin<Project> {

    private lateinit var sourceSets: SourceSetContainer

    override fun apply(project: Project) {
        project.extensions.create("fairy", FairyExtension::class.java)
        this.configureRepositories(project)

        project.plugins.apply(JavaBasePlugin::class.java)
        project.plugins.apply(FairyResourcePlugin::class.java)

        sourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
        project.plugins.withType(JavaPlugin::class.java) { configurePlugin(project, "java") }
        project.plugins.withType(GroovyPlugin::class.java) { configurePlugin(project, "groovy") }
        project.plugins.withType(ScalaPlugin::class.java) { configurePlugin(project, "scala") }
        project.plugins.withId("org.jetbrains.kotlin.jvm") { configurePlugin(project, "kotlin") }

        withPluginClassOfAction(DependencyManagementPluginAction(), project)
    }

    private fun configurePlugin(project: Project, language: String) {
        sourceSets.all { sourceSet ->
            project.tasks.named(sourceSet.getCompileTaskName(language)) {
                val action = project.objects.newInstance(FairyCompilerAction::class.java)
                it.doLast("fairyCompile", action)
            }
        }
    }

    private fun configureRepositories(project: Project) {
        project.repositories.maven { it.setUrl(UrlConstants.repositoryUrl) }
    }

    private fun withPluginClassOfAction(action: PluginApplicationAction, project: Project) {
        val pluginClass: Class<*>
        try {
            pluginClass = action.pluginClass
        } catch (e: ClassNotFoundException) {
            // ignore
            return
        }

        project.plugins.withType(pluginClass) {
            action.execute(project)
        }
    }

}