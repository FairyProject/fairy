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
import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.nio.file.Path
import javax.inject.Inject

/**
 * Task for running Hytale server.
 *
 * @since 0.7
 * @author LeeGod
 * @see io.fairyproject.gradle.runner.hytale.RunHytaleServerPlugin
 */
open class RunHytaleServerTask @Inject constructor(
    private val version: JavaVersion,
    artifact: HytaleServerArtifact,
    workDirectory: Path,
    extension: RunHytaleServerExtension
) : JavaExec() {

    init {
        // Set main class for Hytale server
        mainClass.set("com.hypixel.hytale.Main")

        // Get project's source set
        val mainSourceSet = project.extensions
            .getByType(JavaPluginExtension::class.java)
            .sourceSets
            .getByName("main")

        // Classpath: HytaleServer.jar + project's runtime classpath (compiled classes + dependencies)
        classpath = project.files(artifact.serverJarPath) + mainSourceSet.runtimeClasspath

        workingDir = workDirectory.toFile()
        standardInput = System.`in`

        // Configure Java toolchain
        javaLauncher.set(javaToolchainService.launcherFor {
            it.languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion))
        })

        // Hytale server requires specific arguments
        args("--assets", artifact.assetsPath.toAbsolutePath().toString())
        args("--bind", extension.bindAddress.get())
        // Point to src/main where manifest.json is generated in resources/
        args("--mods", project.file("src/main").absolutePath)
        args("--auth-mode", extension.authMode.get())

        if (extension.allowOp.get()) {
            args("--allow-op")
        }

        if (extension.disableSentry.get()) {
            args("--disable-sentry")
        }
    }

    override fun getJavaVersion(): JavaVersion {
        return version
    }

}
