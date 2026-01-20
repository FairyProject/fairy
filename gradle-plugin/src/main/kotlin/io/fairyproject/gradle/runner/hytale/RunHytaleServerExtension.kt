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

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Extension for [RunHytaleServerPlugin].
 *
 * @since 0.7
 * @author LeeGod
 * @see RunHytaleServerPlugin
 */
open class RunHytaleServerExtension(objectFactory: ObjectFactory) {

    /**
     * Patchline: "pre-release" (default) or "release".
     */
    val patchline: Property<String> = objectFactory.property(String::class.java).convention("pre-release")

    /**
     * Clean work directory before run.
     */
    val cleanup: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)

    /**
     * JVM arguments.
     */
    val args: ListProperty<String> = objectFactory.listProperty(String::class.java)

    /**
     * Additional mod projects to include.
     */
    val projects: ListProperty<Project> = objectFactory.listProperty(Project::class.java).convention(listOf())

    /**
     * Java version (default Java 25 for Hytale).
     */
    val javaVersion: Property<JavaVersion> = objectFactory.property(JavaVersion::class.java).convention(JavaVersion.VERSION_24)

    /**
     * Bind address (default "0.0.0.0:5520").
     */
    val bindAddress: Property<String> = objectFactory.property(String::class.java).convention("0.0.0.0:5520")

    /**
     * URL for downloading the Hytale Downloader CLI ZIP.
     */
    val downloaderUrl: Property<String> = objectFactory.property(String::class.java)
        .convention("https://downloader.hytale.com/hytale-downloader.zip")

    /**
     * Custom path to hytale-downloader binary (optional, overrides download).
     */
    val downloaderPath: Property<String> = objectFactory.property(String::class.java)

    /**
     * Allow OP commands (default true).
     */
    val allowOp: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(true)

    /**
     * Disable Sentry error reporting (default true).
     */
    val disableSentry: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(true)

    /**
     * Authentication mode: "authenticated", "unauthenticated", or "mixed" (default "authenticated").
     */
    val authMode: Property<String> = objectFactory.property(String::class.java).convention("authenticated")

}
