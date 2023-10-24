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

import org.gradle.api.Action
import org.gradle.api.Task
import java.nio.file.Path
import javax.inject.Inject

/**
 * The action that will automatically write the EULA file.
 * Please note that when you use the plugin, you must agree to the EULA.
 *
 * @since 0.7
 * @author LeeGod
 * @see io.fairyproject.gradle.runner.RunSpigotServerPlugin
 * @see <a href="https://account.mojang.com/documents/minecraft_eula">Minecraft EULA</a>
 */
class WriteEulaAction @Inject constructor(private val workDirectory: Path): Action<Task> {
    override fun execute(t: Task) {
        val path = workDirectory.resolve("eula.txt")
        if (path.toFile().exists())
            return

        path.toFile().writeText("#The server is only used to test plugins.\neula=true")
    }
}