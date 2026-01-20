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

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Artifact tracking for Hytale server files.
 *
 * @since 0.7
 * @author LeeGod
 * @see RunHytaleServerPlugin
 */
class HytaleServerArtifact(private val workDirectory: Path) {

    /**
     * Path to HytaleServer.jar
     */
    val serverJarPath: Path
        get() = workDirectory.resolve("Server/HytaleServer.jar")

    /**
     * Path to Assets.zip
     */
    val assetsPath: Path
        get() = workDirectory.resolve("Assets.zip")

    /**
     * Path to version tracking file
     */
    val versionFilePath: Path
        get() = workDirectory.resolve(".fairy-hytale-version")

    /**
     * Check if server artifacts are ready to run
     */
    val hasArtifact: Boolean
        get() = serverJarPath.exists() && assetsPath.exists()

    /**
     * Get the currently downloaded version, or null if not downloaded
     */
    val currentVersion: String?
        get() = if (versionFilePath.exists()) versionFilePath.readText().trim() else null

}
