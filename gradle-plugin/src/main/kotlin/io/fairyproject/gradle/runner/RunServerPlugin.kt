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

import io.fairyproject.gradle.FairyGradlePlugin
import io.fairyproject.gradle.runner.action.CopySnapshotAction
import io.fairyproject.gradle.runner.action.DownloadBuildToolAction
import io.fairyproject.gradle.runner.action.WriteEulaAction
import io.fairyproject.gradle.runner.download.DownloadsAPI
import io.fairyproject.gradle.runner.download.Projects
import io.fairyproject.gradle.runner.task.PreparePaperTask
import io.fairyproject.gradle.runner.task.PrepareSpigotTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import java.nio.file.Path

/**
 * Plugin for running spigot server. the one click *magic* solution to boot up a test environment.
 *
 * @since 0.7
 * @author LeeGod
 */
open class RunServerPlugin : Plugin<Project> {

    private val group = "runServer"

    override fun apply(project: Project) {
        val extension = project.extensions.create("runServer", RunServerExtension::class.java)
        project.afterEvaluate {
            if (extension.version.isPresent)
                configureProject(extension, project)
        }
    }

    private fun configureProject(
        extension: RunServerExtension,
        project: Project
    ) {
        extension.projects.get().forEach { project ->
            project.afterEvaluate {
                if (!it.plugins.hasPlugin(FairyGradlePlugin::class.java)) {
                    it.logger.warn("Project ${it.name} does not have the FairyProject plugin applied and was included to run spigot server.")
                }
            }
        }

        val workDir = project.projectDir.toPath().resolve("server/work")
        val snapshotDir = project.projectDir.toPath().resolve("server/snapshot")
        val buildToolDir = project.projectDir.toPath().resolve("server/build-tools")
        val paperDir = project.projectDir.toPath().resolve("server/paper")
        val spigotArtifact = ServerJarArtifact("spigot", buildToolDir, extension)
        val paperArtifact = ServerJarArtifact("paper", paperDir, extension)
        val foliaArtifact = ServerJarArtifact("folia", paperDir, extension)

        Files.createDirectories(workDir)
        Files.createDirectories(buildToolDir)
        Files.createDirectories(paperDir)

        configurePrepareSpigotBuild(project, buildToolDir, spigotArtifact, extension)

        val downloadsAPI = DownloadsAPI(DownloadsAPI.PAPER_ENDPOINT)
        configurePreparePaperBuild(project, downloadsAPI, extension, paperArtifact)
        configurePrepareFoliaBuild(project, downloadsAPI, extension, foliaArtifact)
        configureCleanSpigotBuild(project, buildToolDir)
        configureCleanServer(project, workDir)
        configureCopyPluginJar(project, workDir)
        configureRunServer("runSpigotServer", "prepareSpigotBuild", project, spigotArtifact, workDir, snapshotDir, extension)
        configureRunServer("runPaperServer", "preparePaperBuild", project, paperArtifact, workDir, snapshotDir, extension)
        configureRunServer("runFoliaServer", "prepareFoliaBuild", project, foliaArtifact, workDir, snapshotDir, extension)
    }

    private fun configurePrepareFoliaBuild(
        project: Project,
        downloadsAPI: DownloadsAPI,
        extension: RunServerExtension,
        foliaArtifact: ServerJarArtifact
    ) {
        project.tasks.register(
            "prepareFoliaBuild", PreparePaperTask::class.java,
            downloadsAPI,
            Projects.FOLIA,
            extension,
            foliaArtifact
        ).configure {
            it.group = group
        }
    }

    private fun configurePreparePaperBuild(
        project: Project,
        downloadsAPI: DownloadsAPI,
        extension: RunServerExtension,
        paperArtifact: ServerJarArtifact
    ) {
        project.tasks.register(
            "preparePaperBuild", PreparePaperTask::class.java,
            downloadsAPI,
            Projects.PAPER,
            extension,
            paperArtifact
        ).configure {
            it.group = group
        }
    }

    private fun configureCopyPluginJar(project: Project, workDir: Path) {
        project.tasks.register("copyPluginJar", Copy::class.java) {
            it.includeProjectJarCopy(project)
            project.extensions.configure(RunServerExtension::class.java) { extension ->
                extension.projects.get().forEach { project ->
                    it.includeProjectJarCopy(project)
                }
            }

            it.into(workDir.resolve("plugins"))
            it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            it.group = group
        }
    }

    private fun Copy.includeProjectJarCopy(project: Project) {
        val jarTask = if (project.tasks.findByName("shadowJar") != null)
            project.tasks.getByName("shadowJar") as Jar
        else
            project.tasks.getByName("jar") as Jar

        from(jarTask.archiveFile.get()) {
            it.rename { name ->
                "runServer-${project.name}.jar"
            }
        }
        dependsOn(jarTask)
    }

    private fun configureRunServer(
        taskName: String,
        prepareTaskName: String,
        project: Project,
        artifact: ServerJarArtifact,
        workDir: Path,
        snapshotDir: Path,
        extension: RunServerExtension
    ) {
        project.afterEvaluate {
            project.tasks.register(taskName, RunServerTask::class.java, artifact, workDir).configure {
                if (extension.cleanup.get()) {
                    it.dependsOn("cleanServer")
                }
                it.doFirst(WriteEulaAction(workDir))
                it.doFirst(CopySnapshotAction(snapshotDir, workDir))
                it.dependsOn("copyPluginJar")
                it.dependsOn(prepareTaskName)

                it.group = group
                it.args = extension.args.get()
                if (extension.versionIsSameOrNewerThan(1, 15)) {
                    it.args("--nogui")
                }

                val classpathRegistry = ClasspathRegistry()
                classpathRegistry.register(project)

                extension.projects.get().forEach { included ->
                    classpathRegistry.register(included)
                }

                it.systemProperties["io.fairyproject.devtools.classpath"] = classpathRegistry.toString()
            }
        }
    }

    private fun configureCleanServer(project: Project, workDir: Path) {
        project.tasks.register("cleanServer") {
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
        artifact: ServerJarArtifact,
        extension: RunServerExtension
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