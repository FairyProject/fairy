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

package io.fairyproject.gradle.runner.hytale.task

import io.fairyproject.gradle.runner.hytale.HytaleServerArtifact
import io.fairyproject.gradle.runner.hytale.RunHytaleServerExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

/**
 * Task to download the Hytale Downloader CLI.
 *
 * @since 0.7
 * @author LeeGod
 */
open class PrepareHytaleDownloaderTask @Inject constructor(
    private val downloaderDirectory: Path,
    private val artifact: HytaleServerArtifact,
    private val extension: RunHytaleServerExtension
) : DefaultTask() {

    /** OS property constants and utility functions. */
    companion object {
        private const val OS_NAME_PROPERTY = "os.name"
        private const val OS_ARCH_PROPERTY = "os.arch"

        /**
         * Gets the path to the Hytale downloader executable.
         */
        fun getDownloaderPath(downloaderDirectory: Path, extension: RunHytaleServerExtension): Path {
            if (extension.downloaderPath.isPresent) {
                return Path.of(extension.downloaderPath.get())
            }
            val binaryName = if (System.getProperty(OS_NAME_PROPERTY).lowercase().contains("win")) {
                "hytale-downloader.exe"
            } else {
                "hytale-downloader"
            }
            return downloaderDirectory.resolve(binaryName)
        }
    }

    private val downloaderPath: Path
        get() = downloaderDirectory.resolve(getDownloaderBinaryName())

    init {
        // If custom path is provided, or downloader already exists, skip
        if (extension.downloaderPath.isPresent || downloaderPath.exists()) {
            enabled = false
        }
    }

    /**
     * Downloads and prepares the Hytale Downloader CLI if not already present.
     */
    @TaskAction
    fun prepareDownloader() {
        if (extension.downloaderPath.isPresent) {
            println("Using custom downloader path: ${extension.downloaderPath.get()}")
            return
        }

        // No native Hytale downloader is published for macOS.
        // Fall back to detecting a pre-existing HytaleServer.jar instead.
        if (isMacOS()) {
            if (artifact.serverJarPath.exists()) {
                println("macOS detected: no downloader available, using existing server jar at: ${artifact.serverJarPath}")
            } else {
                error(
                    "macOS detected: no Hytale downloader is available for macOS, and no existing " +
                        "HytaleServer.jar was found at: ${artifact.serverJarPath}. " +
                        "Please manually place the Hytale server files there before running the server."
                )
            }
            return
        }

        if (downloaderPath.exists()) {
            println("Hytale downloader already exists at: $downloaderPath")
            return
        }

        val downloadUrl = extension.downloaderUrl.get()
        println("Downloading Hytale Downloader CLI from: $downloadUrl")

        downloaderDirectory.createDirectories()

        // Download the ZIP file
        val zipPath = downloaderDirectory.resolve("hytale-downloader.zip")
        URI(downloadUrl).toURL().openStream().use { input ->
            Files.copy(input, zipPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }

        // Extract the appropriate binary
        extractDownloader(zipPath)

        // Clean up ZIP file
        zipPath.deleteIfExists()

        // Set executable permissions on Unix systems
        if (!isWindows()) {
            downloaderPath.toFile().setExecutable(true)
        }

        println("Downloaded Hytale Downloader CLI to: $downloaderPath")
    }

    private fun extractDownloader(zipPath: Path) {
        val targetBinaryName = getDownloaderBinaryNameInZip()

        ZipFile(zipPath.toFile()).use { zip ->
            val entry = zip.entries().asSequence().find { entry ->
                entry.name.endsWith(targetBinaryName) || entry.name == targetBinaryName
            } ?: error("Could not find $targetBinaryName in the downloaded ZIP. Available entries: ${
                zip.entries().asSequence().map { it.name }.toList()
            }")

            zip.getInputStream(entry).use { input ->
                Files.copy(input, downloaderPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun getDownloaderBinaryName(): String {
        return if (isWindows()) "hytale-downloader.exe" else "hytale-downloader"
    }

    private fun getDownloaderBinaryNameInZip(): String {
        val os = getOsName()
        val arch = getArchName()
        val ext = if (isWindows()) ".exe" else ""
        return "hytale-downloader-$os-$arch$ext"
    }

    private fun getOsName(): String {
        val osName = System.getProperty(OS_NAME_PROPERTY).lowercase()
        return when {
            osName.contains("win") -> "windows"
            osName.contains("mac") || osName.contains("darwin") -> "macos"
            osName.contains("linux") -> "linux"
            else -> error("Unsupported operating system: $osName")
        }
    }

    private fun getArchName(): String {
        val arch = System.getProperty(OS_ARCH_PROPERTY).lowercase()
        return when {
            arch.contains("amd64") || arch.contains("x86_64") -> "amd64"
            arch.contains("aarch64") || arch.contains("arm64") -> "arm64"
            else -> error("Unsupported architecture: $arch")
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty(OS_NAME_PROPERTY).lowercase().contains("win")
    }

    private fun isMacOS(): Boolean {
        val osName = System.getProperty(OS_NAME_PROPERTY).lowercase()
        return osName.contains("mac") || osName.contains("darwin")
    }
}
