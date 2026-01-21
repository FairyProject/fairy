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

import com.google.gson.GsonBuilder
import io.fairyproject.gradle.constants.ClassConstants
import io.fairyproject.gradle.extension.FairyExtension
import io.fairyproject.gradle.extension.property.HytaleAuthor
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.IOException
import java.util.jar.JarFile

/**
 * Task for generating Hytale manifest.json into the build output directory.
 * This allows running the server with compiled classes instead of a JAR.
 *
 * @since 0.7
 * @author LeeGod
 */
abstract class GenerateHytaleManifestTask : DefaultTask() {

    /** Constants used for class file scanning. */
    companion object {
        private const val CLASS_FILE_EXTENSION = ".class"
    }

    @get:InputDirectory
    abstract val classesDir: DirectoryProperty

    @get:Classpath
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val projectVersion: Property<String>

    @get:Input
    abstract val projectDescription: Property<String>

    // FairyExtension inputs - these affect the generated manifest
    @get:Input
    @get:org.gradle.api.tasks.Optional
    abstract val fairyName: Property<String>

    @get:Input
    @get:org.gradle.api.tasks.Optional
    abstract val fairyMainPackage: Property<String>

    @get:Input
    @get:org.gradle.api.tasks.Optional
    abstract val fairyFairyPackage: Property<String>

    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Generates the Hytale manifest.json and fairy.json files.
     */
    @TaskAction
    fun generate() {
        val extension = project.extensions.findByType(FairyExtension::class.java)

        // Find HytalePlugin class
        val hytalePluginClass = findHytalePluginClass() ?: run {
            logger.warn("[Fairy] No HytalePlugin class found, skipping manifest generation")
            return
        }

        // Find main class (with @FairyLaunch annotation)
        val mainClass = findMainClass() ?: run {
            logger.warn("[Fairy] No main class with @FairyLaunch found, skipping fairy.json generation")
            return
        }

        val outputDirectory = outputDir.get().asFile
        outputDirectory.mkdirs()

        // Generate manifest.json
        val hytaleProps = extension?.hytalePropertiesRaw() ?: emptyMap()
        val manifest = buildManifest(hytalePluginClass, hytaleProps)
        val manifestFile = File(outputDirectory, "manifest.json")
        manifestFile.writeText(gson.toJson(manifest))
        logger.lifecycle("[Fairy] Generated Hytale manifest.json at ${manifestFile.absolutePath}")

        // Generate fairy.json
        val fairyJson = buildFairyJson(mainClass)
        val fairyFile = File(outputDirectory, "fairy.json")
        fairyFile.writeText(gson.toJson(fairyJson))
        logger.lifecycle("[Fairy] Generated fairy.json at ${fairyFile.absolutePath}")
    }

    private fun findMainClass(): String? {
        val classesDirectory = classesDir.get().asFile
        if (!classesDirectory.exists()) return null

        // First, find the Plugin/Application interface class from runtime classpath
        val pluginInterfaceClass = findPluginInterfaceClass()

        classesDirectory.walkTopDown()
            .filter { it.isFile && it.name.endsWith(CLASS_FILE_EXTENSION) }
            .forEach { classFile ->
                val className = findMainClassInFile(classFile, pluginInterfaceClass)
                if (className != null) return className
            }

        return null
    }

    private fun findPluginInterfaceClass(): String? {
        // Look for Plugin or Application class with @FairyInternalIdentityMeta
        for (jarFile in runtimeClasspath.files.filter { it.isFile && it.extension == "jar" }) {
            val result = findPluginInterfaceInJar(jarFile)
            if (result != null) return result
        }
        return null
    }

    private fun findPluginInterfaceInJar(jarFile: File): String? {
        val jar = try {
            JarFile(jarFile)
        } catch (e: IOException) {
            logger.debug("Could not read JAR file: ${jarFile.name}", e)
            return null
        }

        return jar.use { j ->
            j.entries().asSequence()
                .filter { it.name.endsWith(CLASS_FILE_EXTENSION) }
                .filter {
                    val simpleName = it.name.substringAfterLast("/").removeSuffix(CLASS_FILE_EXTENSION)
                    simpleName == "Plugin" || simpleName == "Application"
                }
                .mapNotNull { entry -> checkForInternalMeta(j, entry) }
                .firstOrNull()
        }
    }

    private fun checkForInternalMeta(jar: JarFile, entry: java.util.zip.ZipEntry): String? {
        val bytes = jar.getInputStream(entry).readBytes()
        val classNode = ClassNode()
        ClassReader(bytes).accept(classNode, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

        val hasInternalMeta = classNode.visibleAnnotations?.any {
            it.desc.contains(ClassConstants.INTERNAL_META)
        } ?: false

        return if (hasInternalMeta) classNode.name else null
    }

    private fun findMainClassInFile(classFile: File, pluginInterfaceClass: String?): String? {
        val bytes = classFile.readBytes()
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()

        classReader.accept(
            classNode,
            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )

        // Skip abstract classes and interfaces
        if (classNode.access and Opcodes.ACC_ABSTRACT != 0) return null
        if (classNode.access and Opcodes.ACC_INTERFACE != 0) return null

        // Check if this class has @FairyLaunch annotation
        val hasFairyLaunch = classNode.visibleAnnotations?.any {
            it.desc.contains(ClassConstants.FAIRY_LAUNCH)
        } ?: false

        // Check if this class extends Plugin or Application
        val extendsPluginInterface = pluginInterfaceClass != null && classNode.superName == pluginInterfaceClass

        if (!hasFairyLaunch && !extendsPluginInterface) return null

        return classNode.name.replace("/", ".")
    }

    private fun buildFairyJson(mainClass: String): Map<String, Any?> {
        val json = mutableMapOf<String, Any?>()
        json["name"] = fairyName.orNull ?: projectName.get()
        json["mainClass"] = mainClass
        fairyMainPackage.orNull?.let { json["shadedPackage"] = it }
        fairyFairyPackage.orNull?.let { json["fairyPackage"] = it }
        return json
    }

    private fun findHytalePluginClass(): String? {
        // First, scan project's compiled classes
        val classesDirectory = classesDir.get().asFile
        if (classesDirectory.exists()) {
            val result = classesDirectory.walkTopDown()
                .filter { it.isFile && it.name.endsWith(CLASS_FILE_EXTENSION) }
                .mapNotNull { findHytalePluginInBytes(it.readBytes()) }
                .firstOrNull()
            if (result != null) return result
        }

        // Then, scan runtime classpath JARs (for HytalePlugin from hytale-bootstrap)
        return runtimeClasspath.files
            .filter { it.isFile && it.extension == "jar" }
            .mapNotNull { findHytalePluginInJar(it) }
            .firstOrNull()
    }

    private fun findHytalePluginInJar(jarFile: File): String? {
        val jar = try {
            JarFile(jarFile)
        } catch (e: IOException) {
            logger.debug("Could not read JAR file: ${jarFile.name}", e)
            return null
        }

        return jar.use { j ->
            j.entries().asSequence()
                .filter { it.name.endsWith("HytalePlugin${CLASS_FILE_EXTENSION}") }
                .mapNotNull { entry ->
                    val bytes = j.getInputStream(entry).readBytes()
                    findHytalePluginInBytes(bytes)
                }
                .firstOrNull()
        }
    }

    private fun findHytalePluginInBytes(bytes: ByteArray): String? {
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()

        classReader.accept(
            classNode,
            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )

        // Check if this class has @FairyInternalIdentityMeta annotation
        val hasInternalMeta = classNode.visibleAnnotations?.any {
            it.desc.contains(ClassConstants.INTERNAL_META)
        } ?: false

        if (!hasInternalMeta) return null

        // Check if the class name ends with HytalePlugin
        val simpleName = classNode.name.substringAfterLast("/")
        if (simpleName != "HytalePlugin") return null

        // Skip abstract classes and interfaces
        if (classNode.access and Opcodes.ACC_ABSTRACT != 0) return null
        if (classNode.access and Opcodes.ACC_INTERFACE != 0) return null

        return classNode.name.replace("/", ".")
    }

    private fun buildManifest(
        hytalePluginClass: String,
        hytaleProps: Map<String, Any>
    ): Map<String, Any> {
        val manifest = mutableMapOf<String, Any>()

        manifest["Main"] = hytalePluginClass
        manifest["Group"] = (hytaleProps["Group"] as? String)?.takeIf { it.isNotEmpty() } ?: "io.fairyproject"
        manifest["Name"] = fairyName.orNull ?: projectName.get()
        manifest["Version"] = normalizeToSemver(projectVersion.get())
        manifest["Description"] = projectDescription.get()

        // Authors
        val authors = hytaleProps["Authors"]
        manifest["Authors"] = if (authors is List<*> && authors.isNotEmpty()) {
            authors.map { author ->
                when (author) {
                    is HytaleAuthor -> mapOf(
                        "Name" to author.name,
                        "Email" to author.email,
                        "Url" to author.url
                    )
                    else -> mapOf("Name" to "", "Email" to "", "Url" to "")
                }
            }
        } else {
            emptyList<Map<String, String>>()
        }

        manifest["Website"] = hytaleProps["Website"] as? String ?: ""
        manifest["ServerVersion"] = hytaleProps["ServerVersion"] as? String ?: ""
        manifest["Dependencies"] = hytaleProps["Dependencies"] as? Map<*, *> ?: emptyMap<String, String>()
        manifest["OptionalDependencies"] = hytaleProps["OptionalDependencies"] as? Map<*, *> ?: emptyMap<String, String>()
        manifest["LoadBefore"] = hytaleProps["LoadBefore"] as? Map<*, *> ?: emptyMap<String, String>()
        manifest["DisabledByDefault"] = hytaleProps["DisabledByDefault"] as? Boolean ?: false
        manifest["IncludesAssetPack"] = hytaleProps["IncludesAssetPack"] as? Boolean ?: true
        manifest["SubPlugins"] = hytaleProps["SubPlugins"] as? List<*> ?: emptyList<String>()

        return manifest
    }

    private fun normalizeToSemver(version: String): String {
        if (version.isBlank() || version == "unspecified") {
            return "0.0.1"
        }

        val semverRegex = Regex("""^\d+\.\d+\.\d+(-[\w.]+)?(\+[\w.]+)?$""")
        if (semverRegex.matches(version)) {
            return version
        }

        val numbers = Regex("""\d+""").findAll(version).map { it.value }.toList()
        return when {
            numbers.size >= 3 -> "${numbers[0]}.${numbers[1]}.${numbers[2]}"
            numbers.size == 2 -> "${numbers[0]}.${numbers[1]}.0"
            numbers.size == 1 -> "${numbers[0]}.0.0"
            else -> "0.0.1"
        }
    }
}
