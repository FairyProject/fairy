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

package io.fairyproject.gradle.runner.hytale

import io.fairyproject.gradle.FairyGradlePlugin
import io.fairyproject.gradle.runner.ClasspathRegistry
import io.fairyproject.gradle.runner.hytale.action.CopyHytaleSnapshotAction
import io.fairyproject.gradle.runner.hytale.task.PrepareHytaleBuildTask
import io.fairyproject.gradle.runner.hytale.task.PrepareHytaleDownloaderTask
import io.fairyproject.gradle.runner.hytale.task.RunHytaleServerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import java.nio.file.Path

/**
 * Plugin for running Hytale server. One-click solution to boot up a Hytale test environment.
 *
 * @since 0.7
 * @author LeeGod
 */
open class RunHytaleServerPlugin : Plugin<Project> {

    private val group = "runHytaleServer"
    private lateinit var project: Project
    private lateinit var extension: RunHytaleServerExtension

    override fun apply(project: Project) {
        this.project = project
        extension = project.extensions.create("runHytaleServer", RunHytaleServerExtension::class.java)

        project.afterEvaluate {
            configureProject(extension, project)
        }
    }

    private fun configureProject(
        extension: RunHytaleServerExtension,
        project: Project
    ) {
        extension.projects.get().forEach { includedProject ->
            includedProject.afterEvaluate {
                if (!it.plugins.hasPlugin(FairyGradlePlugin::class.java)) {
                    it.logger.warn("Project ${it.name} does not have the FairyProject plugin applied and was included to run Hytale server.")
                }
            }
        }

        // Directory structure
        val baseDir = project.projectDir.toPath().resolve("server/hytale")
        val downloaderDir = baseDir.resolve("downloader")
        val downloadsDir = baseDir.resolve("downloads")
        val workDir = baseDir.resolve("work")
        val snapshotDir = baseDir.resolve("snapshot")

        // Create directories
        Files.createDirectories(downloaderDir)
        Files.createDirectories(downloadsDir)
        Files.createDirectories(workDir)
        Files.createDirectories(snapshotDir)

        val artifact = HytaleServerArtifact(workDir)

        configurePrepareHytaleDownloader(downloaderDir)
        configurePrepareHytaleBuild(downloaderDir, downloadsDir, workDir, artifact)
        configureCopyHytaleModJar(workDir)
        configureCleanHytaleServer(workDir)
        configureRunHytaleServer(artifact, workDir, snapshotDir)
    }

    private fun configurePrepareHytaleDownloader(downloaderDir: Path) {
        project.tasks.register(
            "prepareHytaleDownloader",
            PrepareHytaleDownloaderTask::class.java,
            downloaderDir,
            extension
        ).configure {
            it.group = group
            it.description = "Downloads the Hytale Downloader CLI"
        }
    }

    private fun configurePrepareHytaleBuild(
        downloaderDir: Path,
        downloadsDir: Path,
        workDir: Path,
        artifact: HytaleServerArtifact
    ) {
        project.tasks.register(
            "prepareHytaleBuild",
            PrepareHytaleBuildTask::class.java,
            downloaderDir,
            downloadsDir,
            workDir,
            artifact,
            extension
        ).configure {
            it.group = group
            it.description = "Downloads and extracts Hytale server files"
            it.dependsOn("prepareHytaleDownloader")
        }
    }

    private fun configureCopyHytaleModJar(workDir: Path) {
        project.tasks.register("copyHytaleModJar", Copy::class.java) {
            it.includeProjectJarCopy(project)
            project.extensions.configure(RunHytaleServerExtension::class.java) { ext ->
                ext.projects.get().forEach { includedProject ->
                    it.includeProjectJarCopy(includedProject)
                }
            }

            it.into(workDir.resolve("mods"))
            it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            it.group = group
            it.description = "Copies mod JARs to the Hytale server mods directory"
        }
    }

    private fun Copy.includeProjectJarCopy(project: Project) {
        val jarTask = if (project.tasks.findByName("shadowJar") != null)
            project.tasks.getByName("shadowJar") as Jar
        else
            project.tasks.getByName("jar") as Jar

        from(jarTask.archiveFile.get()) {
            it.rename { _ ->
                "runServer-${project.name}.jar"
            }
        }
        dependsOn(jarTask)
    }

    private fun configureCleanHytaleServer(workDir: Path) {
        project.tasks.register("cleanHytaleServer", Delete::class.java) {
            it.delete(workDir.toFile().listFiles()?.filter { file ->
                // Don't delete mods directory on clean
                file.name != "mods" && file.name != ".fairy-hytale-version"
            } ?: emptyList<Any>())
            it.group = group
            it.description = "Cleans the Hytale server work directory"
        }
    }

    private fun configureRunHytaleServer(
        artifact: HytaleServerArtifact,
        workDir: Path,
        snapshotDir: Path
    ) {
        project.afterEvaluate {
            project.tasks.register(
                "runHytaleServer",
                RunHytaleServerTask::class.java,
                extension.javaVersion.get(),
                artifact,
                workDir,
                extension
            ).configure {
                if (extension.cleanup.get()) {
                    it.dependsOn("cleanHytaleServer")
                }
                it.doFirst(CopyHytaleSnapshotAction(snapshotDir, workDir))
                it.dependsOn("copyHytaleModJar")
                it.dependsOn("prepareHytaleBuild")

                it.group = group
                it.description = "Runs the Hytale server with mods"
                it.jvmArgs = extension.args.get()

                val classpathRegistry = ClasspathRegistry()
                classpathRegistry.register(project)

                extension.projects.get().forEach { included ->
                    classpathRegistry.register(included)
                }

                it.systemProperties["io.fairyproject.devtools.classpath"] = classpathRegistry.toString()
            }
        }
    }

}
