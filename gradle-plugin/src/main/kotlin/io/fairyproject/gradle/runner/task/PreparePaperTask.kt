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

package io.fairyproject.gradle.runner.task

import io.fairyproject.gradle.runner.RunServerExtension
import io.fairyproject.gradle.runner.ServerJarArtifact
import io.fairyproject.gradle.runner.download.DownloadsAPI
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.URL
import javax.inject.Inject

/**
 * Task to prepare jars from Paper APIs.
 *
 * @since 0.7.0
 * @property downloadsAPI Paper downloads API
 */
open class PreparePaperTask @Inject constructor(
    private val downloadsAPI: DownloadsAPI,
    private val projectName: String,
    private val extension: RunServerExtension,
    private val artifact: ServerJarArtifact
) : DefaultTask() {

    init {
        if (artifact.hasArtifact) {
            println("Paper jar already exists.")
            enabled = false
        }
    }

    /**
     * Download Paper jar.
     */
    @TaskAction
    fun preparePaper() {
        val version = extension.version.get()
        val response = downloadsAPI.version(projectName, version)
        val buildNumber = response.builds.last()
        val download = downloadsAPI.build(projectName, version, buildNumber).downloads["application"]
            ?: error("No application download found for $projectName $version $buildNumber")
        val downloadURL = URL(downloadsAPI.downloadURL(projectName, version, buildNumber, download))

        println("Downloading Paper jar... ($downloadURL)")
        downloadURL.openStream().use { input ->
            val bytes = input.readBytes()

            artifact.artifactPath.toFile().writeBytes(bytes)
        }

        println("Downloaded Paper jar.")
    }

}