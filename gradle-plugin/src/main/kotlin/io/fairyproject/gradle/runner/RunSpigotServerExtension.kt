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

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

open class RunSpigotServerExtension(objectFactory: ObjectFactory) {

    val version: Property<String> = objectFactory.property(String::class.java)
    val cleanup: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)
    val args: ListProperty<String> = objectFactory.listProperty(String::class.java)
    val projects: ListProperty<Project> = objectFactory.listProperty(Project::class.java).convention(listOf())
    val buildToolUrl: Property<String> = objectFactory.property(String::class.java).convention("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")

    /**
     * @author runtask gradle plugin
     */
    fun versionIsSameOrNewerThan(vararg other: Int): Boolean {
        val minecraft = version.get().split(".").map {
            try {
                it.toInt()
            } catch (ex: NumberFormatException) {
                return true
            }
        }

        for ((current, target) in minecraft zip other.toList()) {
            if (current < target) return false
            if (current > target) return true
            // If equal, check next subversion
        }

        // version is same
        return true
    }

}
