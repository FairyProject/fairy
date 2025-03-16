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
        val projectInfo = ProjectInfo(project.name, project.version.toString(), project.description ?: "")

        val hasBukkitPlatform by lazy {
            project.configurations
                .filter { it.isCanBeResolved }
                .flatMap { it.dependencies }
                .any { it.isBukkitPlatform }
        }

        val action = project.objects.newInstance(FairyResourceAction::class.java).apply {
            this.extension.set(extension)
            this.projectInfo.set(projectInfo)
            this.hasBukkitPlatform.set(hasBukkitPlatform)
        }

        jar.doLast("fairyResource", action)
    }

    private val Dependency.isBukkitPlatform: Boolean
        get() = group == "io.fairyproject" &&
                name in listOf("bukkit-platform", "bukkit-bundles", "bukkit-bootstrap")
}