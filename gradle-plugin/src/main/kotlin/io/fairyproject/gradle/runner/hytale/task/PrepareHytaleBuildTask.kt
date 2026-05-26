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
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Task to download and extract Hytale server files using the Hytale Downloader CLI.
 *
 * @since 0.7
 * @author LeeGod
 */
open class PrepareHytaleBuildTask @Inject constructor(
    private val downloaderDirectory: Path,
    private val downloadsDirectory: Path,
    private val workDirectory: Path,
    private val artifact: HytaleServerArtifact,
    private val extension: RunHytaleServerExtension
) : DefaultTask() {

    /**
     * Downloads and extracts Hytale server files using the Hytale Downloader CLI.
     */
    @TaskAction
    fun prepareBuild() {
        // If artifact already exists, skip (always downloads latest)
        if (artifact.hasArtifact) {
            println("Hytale server already prepared. Delete ${artifact.versionFilePath} to force re-download.")
            return
        }

        // No native Hytale downloader is published for macOS — it cannot download the server.
        if (isMacOS()) {
            error(
                "macOS detected: no Hytale downloader is available for macOS. " +
                    "Please manually place the Hytale server files (Server/HytaleServer.jar and Assets.zip) in: $workDirectory"
            )
        }

        // Run the downloader
        val downloaderPath = PrepareHytaleDownloaderTask.getDownloaderPath(downloaderDirectory, extension)
        if (!downloaderPath.exists()) {
            error("Hytale downloader not found at: $downloaderPath. Run prepareHytaleDownloader first.")
        }

        downloadsDirectory.createDirectories()

        val downloadedZip = downloadsDirectory.resolve("hytale-server.zip")
        runDownloader(downloaderPath, downloadedZip)

        // Extract the downloaded ZIP
        extractServerFiles(downloadedZip.toFile())

        // Get version from -print-version and write version file
        val version = getVersion(downloaderPath)
        artifact.versionFilePath.writeText(version)

        // Clean up downloaded ZIP
        downloadedZip.deleteIfExists()

        println("Hytale server prepared successfully (version: $version).")
    }

    private fun runDownloader(downloaderPath: Path, outputZip: Path) {
        println("Running Hytale Downloader CLI...")

        val command = mutableListOf(downloaderPath.toAbsolutePath().toString())
        command.add("-download-path")
        command.add(outputZip.toAbsolutePath().toString())
        command.add("-patchline")
        command.add(extension.patchline.get())
//        command.add("-skip-update-check")

        println("Executing: ${command.joinToString(" ")}")

        val result = executeDownloader(command)

        if (result.exitCode != 0) {
            if (result.hasExpiredToken) {
                val credentialsFile = downloaderDirectory.resolve(".hytale-downloader-credentials.json")
                if (credentialsFile.exists()) {
                    println("Detected expired OAuth2 refresh token. Deleting credentials to re-authenticate...")
                    credentialsFile.deleteIfExists()

                    // Retry — downloader will prompt for fresh OAuth login
                    val retryResult = executeDownloader(command)
                    if (retryResult.exitCode != 0) {
                        error("Hytale Downloader failed with exit code: ${retryResult.exitCode} after re-authentication attempt")
                    }
                    return
                }
            }
            error("Hytale Downloader failed with exit code: ${result.exitCode}")
        }
    }

    private data class DownloaderResult(val exitCode: Int, val hasExpiredToken: Boolean)

    private fun executeDownloader(command: List<String>): DownloaderResult {
        val processBuilder = ProcessBuilder(command)
            .directory(downloaderDirectory.toFile())
            .redirectErrorStream(true)

        val process = processBuilder.start()

        var hasExpiredToken = false

        // Forward output to console in real-time for OAuth authentication
        val outputThread = Thread {
            process.inputStream.bufferedReader().forEachLine { line ->
                println(line)
                if (line.contains("invalid_grant") || line.contains("refresh token", ignoreCase = true)) {
                    hasExpiredToken = true
                }
            }
        }
        outputThread.start()

        val exitCode = process.waitFor()
        outputThread.join()

        return DownloaderResult(exitCode, hasExpiredToken)
    }

    private fun getVersion(downloaderPath: Path): String {
        val command = listOf(
            downloaderPath.toAbsolutePath().toString(),
            "-print-version",
            "-patchline",
            extension.patchline.get(),
            "-skip-update-check"
        )

        val process = ProcessBuilder(command)
            .directory(downloaderDirectory.toFile())
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText().trim()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            println("Warning: Could not get version info, using 'unknown'")
            return "unknown"
        }

        return output.ifBlank { "unknown" }
    }

    private fun extractServerFiles(zipFile: File) {
        println("Extracting server files from: ${zipFile.name}")

        workDirectory.createDirectories()

        // Preserve mods directory if it exists
        val modsDir = workDirectory.resolve("mods")
        val modsBackup = backupModsDirectory(modsDir)

        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence()
                .filter { shouldExtractEntry(it.name) }
                .forEach { entry -> extractZipEntry(zip, entry) }
        }

        restoreModsDirectory(modsBackup, modsDir)

        println("Server files extracted to: $workDirectory")
    }

    private fun backupModsDirectory(modsDir: Path): Path? {
        if (!modsDir.exists()) return null
        val backup = Files.createTempDirectory("fairy-hytale-mods-backup")
        modsDir.toFile().copyRecursively(backup.toFile(), overwrite = true)
        return backup
    }

    private fun shouldExtractEntry(entryName: String): Boolean {
        if (entryName.startsWith("Client/")) return false
        if (entryName.startsWith("mods/") || entryName == "mods") return false
        return true
    }

    private fun extractZipEntry(zip: ZipFile, entry: java.util.zip.ZipEntry) {
        val targetPath = workDirectory.resolve(entry.name)
        if (entry.isDirectory) {
            targetPath.createDirectories()
        } else {
            targetPath.parent?.createDirectories()
            zip.getInputStream(entry).use { input ->
                Files.copy(input, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun restoreModsDirectory(modsBackup: Path?, modsDir: Path) {
        if (modsBackup == null) return
        modsDir.createDirectories()
        modsBackup.toFile().copyRecursively(modsDir.toFile(), overwrite = true)
        modsBackup.toFile().deleteRecursively()
    }

    private fun isMacOS(): Boolean {
        val osName = System.getProperty("os.name").lowercase()
        return osName.contains("mac") || osName.contains("darwin")
    }

}
