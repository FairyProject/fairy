package io.fairyproject.gradle.resource

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

/**
 * The resource plugin.
 */
class FairyResourcePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType(Jar::class.java) { configureJarTask(project, it) }
    }

    private fun configureJarTask(project: Project, jar: Jar) {
        val action = project.objects.newInstance(FairyResourceAction::class.java)
        jar.doLast("fairyResource", action)
    }
}