package io.fairyproject.gradle

import io.fairyproject.gradle.compiler.FairyCompilerAction
import io.fairyproject.gradle.constants.UrlConstants
import io.fairyproject.gradle.dependency.DependencyManagementPluginAction
import io.fairyproject.gradle.extension.FairyExtension
import io.fairyproject.gradle.resource.FairyResourcePlugin
import io.fairyproject.gradle.runner.RunServerPlugin
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
    private lateinit var extension: FairyExtension

    companion object {
        private const val OVERRIDE_REPOS_PROP = "fairy.repositories.override"
        private const val ADD_DEFAULT_REPOS_PROP = "fairy.repositories.addDefault"
    }

    override fun apply(project: Project) {
        extension = project.extensions.create("fairy", FairyExtension::class.java)
        
        configureRepositories(project)

        project.plugins.apply(JavaBasePlugin::class.java)
        project.plugins.apply(FairyResourcePlugin::class.java)
        project.plugins.apply(RunServerPlugin::class.java)

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
        // 檢查是否為真實專案
        if (project.name == "gradle-kotlin-dsl-accessors") {
            project.logger.debug("Skipping repository configuration for internal project: ${project.name}")
            return
        }

        project.logger.warn("Project name: ${project.name}")
        project.logger.warn("Project path: ${project.path}")

        val overrideRepos = project.findProperty(OVERRIDE_REPOS_PROP)?.toString()?.toBoolean() == true
        val addDefaultRepos = project.findProperty(ADD_DEFAULT_REPOS_PROP)?.toString()?.toBoolean() != false

        project.logger.warn("overrideRepositories: $overrideRepos")
        project.logger.warn("addDefaultRepositories: $addDefaultRepos")

        if (overrideRepos) {
            project.repositories.clear()
        }

        if (addDefaultRepos) {
            // 列出當前的 repositories
            project.logger.info("列出專案 repositories:")
            project.repositories.forEach {
                project.logger.info(it.toString())
            }

            // 添加 Fairy repository
            try {
                project.repositories.addFirst(project.repositories.maven {
                    it.setUrl(UrlConstants.repositoryUrl)
                })
                project.logger.info("已添加 Fairy repository: ${UrlConstants.repositoryUrl}")
            } catch (e: Exception) {
                project.logger.info("無法添加 Fairy repository: ${e.message}")
            }

            // 添加 CodeMC Release repository
            try {
                project.repositories.addFirst(project.repositories.maven {
                    it.setUrl(UrlConstants.codeMcReleaseRepositoryUrl)
                    it.content {
                        it.includeGroup("com.github.retrooper")
                    }
                })
                project.logger.info("已添加 CodeMC Release repository: ${UrlConstants.codeMcReleaseRepositoryUrl}")
            } catch (e: Exception) {
                project.logger.info("無法添加 CodeMC Release repository: ${e.message}")
            }

            // 添加 CodeMC Snapshot repository
            try {
                project.repositories.addFirst(project.repositories.maven {
                    it.setUrl(UrlConstants.codeMcSnapshotRepositoryUrl)
                    it.content {
                        it.includeGroup("com.github.retrooper")
                    }
                })
                project.logger.info("已添加 CodeMC Snapshot repository: ${UrlConstants.codeMcSnapshotRepositoryUrl}")
            } catch (e: Exception) {
                project.logger.info("無法添加 CodeMC Snapshot repository: ${e.message}")
            }
        }
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