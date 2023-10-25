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

package io.fairyproject.gradle.runner

import io.fairyproject.gradle.extension.FairyExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

/**
 * Classpath registry of the plugin for server runner.
 *
 * @since 0.7
 * @author LeeGod
 * @see RunSpigotServerPlugin
 */
class ClasspathRegistry {

    private val classpath = mutableMapOf<String, MutableList<String>>()

    /**
     * Register a classpath.
     */
    fun register(name: String, path: String) {
        val list = classpath.computeIfAbsent(name) { mutableListOf() }
        list += path
    }

    /**
     * Register a classpath from a project.
     */
    fun register(project: Project) {
        val fairyExtension = project.extensions.findByType(FairyExtension::class.java) ?: return
        val name = fairyExtension.name.get()

        project.extensions.configure(JavaPluginExtension::class.java) { java ->
            val path = java.sourceSets.getByName("main").output.classesDirs.asPath

            path.split(":").forEach {
                register(name, it)
            }
        }
    }

    override fun toString(): String = classpath
        .map { "${it.key}|${it.value.joinToString(",")}" }
        .joinToString(":")

}