/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.gradle.runner

import io.fairyproject.gradle.runner.action.DownloadBuildToolAction
import io.fairyproject.gradle.runner.task.PrepareSpigotTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import java.nio.file.Path

open class RunSpigotServerPlugin : Plugin<Project> {

    private val group = "runSpigot"

    override fun apply(project: Project) {
        val extension = project.extensions.create("runSpigotServer", RunSpigotServerExtension::class.java)
        project.afterEvaluate {
            if (!extension.version.isPresent)
                return@afterEvaluate

            val workDir = project.projectDir.toPath().resolve("spigotServer/work")
            val buildToolDir = project.projectDir.toPath().resolve("spigotServer/build-tools")
            val artifact = SpigotJarArtifact(buildToolDir, extension)

            Files.createDirectories(workDir)
            Files.createDirectories(buildToolDir)

            configurePrepareSpigotBuild(project, buildToolDir, artifact, extension)
            configureCleanSpigotBuild(project, buildToolDir)
            configureCleanSpigotServer(project, workDir)
            configureCopyPluginJar(project, workDir)
            configureRunSpigotServer(project, artifact, workDir, extension)
        }
    }

    private fun configureCopyPluginJar(project: Project, workDir: Path) {
        project.tasks.register("copyPluginJar", Copy::class.java) {
            val jarTask = if (project.tasks.findByName("shadowJar") != null)
                project.tasks.getByName("shadowJar") as Jar
            else
                project.tasks.getByName("jar") as Jar

            it.from(jarTask.archiveFile.get())
            it.into(workDir.resolve("plugins"))
            it.eachFile {
                it.name = "runSpigotServer-plugin.jar"
            }
            it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            it.group = group

            it.dependsOn(jarTask)
        }
    }

    private fun configureRunSpigotServer(
        project: Project,
        artifact: SpigotJarArtifact,
        workDir: Path,
        extension: RunSpigotServerExtension
    ) {
        project.afterEvaluate {
            project.tasks.register("runSpigotServer", RunSpigotServerTask::class.java, artifact, workDir).configure {
                if (extension.cleanup.get()) {
                    it.dependsOn("cleanSpigotServer")
                }
                it.dependsOn("copyPluginJar")
                it.dependsOn("prepareSpigotBuild")
                it.group = group

                project.extensions.configure(JavaPluginExtension::class.java) { java ->
                    val path = java.sourceSets.getByName("main").runtimeClasspath.asPath

                    it.systemProperties["io.fairyproject.devtools.classpath"] = path
                    it.args = extension.args.get()
                }
            }
        }
    }

    private fun configureCleanSpigotServer(project: Project, workDir: Path) {
        project.tasks.register("cleanSpigotServer") {
            it.doLast {
                Files.newDirectoryStream(workDir).use { stream ->
                    stream.forEach { path ->
                        Files.delete(path)
                    }
                }
            }
            it.group = group
        }
    }

    private fun configureCleanSpigotBuild(project: Project, buildToolDir: Path) {
        project.tasks.register("cleanSpigotBuild") {
            it.doLast {
                Files.newDirectoryStream(buildToolDir).use { stream ->
                    stream.forEach { path ->
                        Files.delete(path)
                    }
                }
            }
            it.group = group
        }
    }

    private fun configurePrepareSpigotBuild(
        project: Project,
        buildToolDir: Path,
        artifact: SpigotJarArtifact,
        extension: RunSpigotServerExtension
    ) {
        project.tasks.register(
            "prepareSpigotBuild",
            PrepareSpigotTask::class.java,
            buildToolDir,
            artifact,
            extension
        ).configure {
            it.doFirst(DownloadBuildToolAction(buildToolDir))
            it.group = group
        }
    }
}