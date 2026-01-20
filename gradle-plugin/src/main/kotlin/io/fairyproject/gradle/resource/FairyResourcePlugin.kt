package io.fairyproject.gradle.resource

import io.fairyproject.gradle.extension.FairyExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.jvm.tasks.Jar

/**
 * The resource plugin.
 */
class FairyResourcePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType(Jar::class.java) { configureJarTask(project, it) }
    }

    private fun configureJarTask(project: Project, jar: Jar) {
        val extension = project.extensions.findByType(FairyExtension::class.java) ?: return
        
        val hasBukkitPlatform by lazy {
            project.configurations
                .flatMap { it.dependencies }
                .any { it.isBukkitPlatform }
        }

        val hasHytalePlatform by lazy {
            project.configurations
                .flatMap { it.dependencies }
                .any { it.isHytalePlatform }
        }

        val action = project.objects.newInstance(FairyResourceAction::class.java).apply {
            this.extension.set(extension)
            // Set the value of hasBukkitPlatform, hasHytalePlatform and projectInfo after the project is evaluated
            project.afterEvaluate {
                val projectInfo = ProjectInfo(project.name, project.version.toString(), project.description ?: "")
                this.projectInfo.set(projectInfo)
                this.hasBukkitPlatform.set(hasBukkitPlatform)
                this.hasHytalePlatform.set(hasHytalePlatform)
            }
        }

        jar.doLast("fairyResource", action)
    }

    private val Dependency.isBukkitPlatform: Boolean
        get() = group == "io.fairyproject" &&
                name in listOf("bukkit-platform", "bukkit-bundles", "bukkit-bootstrap")

    private val Dependency.isHytalePlatform: Boolean
        get() = group == "io.fairyproject" &&
                name in listOf("hytale-platform", "hytale-bundles", "hytale-bootstrap")
}