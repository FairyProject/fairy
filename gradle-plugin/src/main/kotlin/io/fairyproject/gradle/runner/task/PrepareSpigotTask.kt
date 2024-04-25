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
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.absolutePathString

/**
 * Task for preparing spigot jar. it will build a spigot using BuildTools with the specified version.
 *
 * @since 0.7
 * @author LeeGod
 * @see io.fairyproject.gradle.runner.RunServerPlugin
 */
open class PrepareSpigotTask @Inject constructor(
    buildToolDirectory: Path,
    artifact: ServerJarArtifact,
    private val extension: RunServerExtension): JavaExec() {

    init {
        if (artifact.hasArtifact) {
            println("Spigot jar already exists.")
            enabled = false
        }

        mainClass.set("-jar")
        args = listOf(buildToolDirectory.resolve("BuildTools.jar").absolutePathString(), "--rev", extension.version.get())
        workingDir = buildToolDirectory.toFile()
        javaLauncher.set(javaToolchainService.launcherFor { it.languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion)) })
    }

    override fun getJavaVersion(): JavaVersion {
        return extension.javaVersion.get()
    }

}