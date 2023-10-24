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
import org.jetbrains.kotlin.konan.file.recursiveCopyTo
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

/**
 * Action for copying the snapshot directory to the work directory.
 *
 * @since 0.7
 * @author LeeGod
 * @see io.fairyproject.gradle.runner.RunSpigotServerPlugin
 */
class CopySnapshotAction(private val snapshotDirectory: Path, private val workDirectory: Path): Action<Task> {
    override fun execute(t: Task) {
        if (!snapshotDirectory.exists())
            return

        // copy the contents of the snapshot directory to the work directory
        snapshotDirectory.listDirectoryEntries().forEach {
            // copy the file to the work directory, the file can be a directory
            if (it.isDirectory()) {
                it.recursiveCopyTo(workDirectory.resolve(it.fileName))
            } else {
                it.copyTo(workDirectory.resolve(it.fileName), true)
            }
        }
    }
}