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

package io.fairyproject.gradle.runner.action

import io.fairyproject.gradle.runner.RunSpigotServerExtension
import org.gradle.api.Action
import org.gradle.api.Task
import java.net.URL
import java.nio.file.Path
import javax.inject.Inject

open class DownloadBuildToolAction @Inject constructor(private val buildToolDirectory: Path): Action<Task> {

    override fun execute(t: Task) {
        val extension = t.project.extensions.getByType(RunSpigotServerExtension::class.java)
        val buildToolFile = buildToolDirectory.resolve("BuildTools.jar")
        if (buildToolFile.toFile().exists()) {
            println("BuildTool already exists.")
            return
        }

        println("Downloading BuildTool...")

        val buildToolUrl = extension.buildToolUrl.get()
        val url = URL(buildToolUrl)

        url.openStream().use { input ->
            val bytes = input.readBytes()

            buildToolFile.toFile().writeBytes(bytes)
        }

        println("Downloaded BuildTool.")
    }

}