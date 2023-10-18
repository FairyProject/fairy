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

import io.fairyproject.gradle.runner.RunSpigotServerExtension
import io.fairyproject.gradle.runner.SpigotJarArtifact
import org.gradle.api.tasks.JavaExec
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.absolutePathString

open class PrepareSpigotTask @Inject constructor(
    buildToolDirectory: Path,
    artifact: SpigotJarArtifact,
    extension: RunSpigotServerExtension): JavaExec() {

    init {
        if (artifact.hasArtifact) {
            println("Spigot jar already exists.")
            enabled = false
        }

        mainClass.set("-jar")
        args = listOf(buildToolDirectory.resolve("BuildTools.jar").absolutePathString(), "--rev", extension.version.get())
        workingDir = buildToolDirectory.toFile()
    }

}