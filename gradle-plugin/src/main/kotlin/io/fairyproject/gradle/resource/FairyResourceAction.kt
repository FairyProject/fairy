package io.fairyproject.gradle.resource

import io.fairyproject.gradle.constants.ClassConstants
import io.fairyproject.gradle.extension.FairyExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.BufferedOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Action for resource plugin.
 */
open class FairyResourceAction : Action<Task> {
    override fun execute(task: Task) {
        val jar = task as Jar
        val project = jar.project
        val extension = project.extensions.getByType(FairyExtension::class.java)
        val file = jar.archiveFile.get().asFile
        val outputFile = kotlin.io.path.createTempFile(file.nameWithoutExtension, file.extension).toFile()

        JarFile(file).use { inJar -> JarOutputStream(BufferedOutputStream(outputFile.outputStream())).use { output ->
            kotlin.runCatching {
                val classMapper = mutableMapOf<ClassType, ClassInfo>()
                val classes = mutableListOf<ClassInfo>()

                // Read input jar information and copy to temporary file
                readJarClasses(inJar, output, classes, classMapper)

                // Second loop to identify main class
                val mainClassInterface = classMapper[ClassType.MAIN_CLASS_INTERFACE]

                classes.forEach {
                    // Avoid module-info
                    val isMainClass = mainClassInterface?.name == it.classNode.superName || hasFairyLaunch(it)
                    if (isMainClass &&
                        !it.classNode.name.contains("module-info") &&
                        // Not abstract
                        it.classNode.access and Opcodes.ACC_ABSTRACT == 0 &&
                        // Not interface
                        it.classNode.access and Opcodes.ACC_INTERFACE == 0) {
                        // the super class was main class interface, so it's main class
                        classMapper[ClassType.MAIN_CLASS] = it
                    }
                }

                classMapper[ClassType.MAIN_CLASS] ?: run {
                    println("[Fairy] Main class not found, no resources will be generated.")
                    return
                }

                classMapper[ClassType.BUKKIT_PLUGIN] ?: run {
                    println("[Fairy] Bukkit plugin class not found, no resources will be generated.")
                    return
                }

                // Generate resource
                FairyResource.ALL.forEach {
                    it.generate(project, extension, classMapper)?.let { resource ->
                        output.putNextEntry(JarEntry(resource.name))
                        output.write(resource.byteArray)
                    }
                }
            }.getOrElse { e ->
                e.printStackTrace()
            }
        }}

        // Write temporary file back to original file
        outputFile.copyTo(file, true)
        outputFile.delete()
    }

    private fun readJarClasses(
        inJar: JarFile,
        output: JarOutputStream,
        classes: MutableList<ClassInfo>,
        classMapper: MutableMap<ClassType, ClassInfo>) {
        for (entry in inJar.entries()) {
            if (this.shouldExcludeFile(entry)) continue
            val bytes = inJar.getInputStream(entry).readBytes()
            // The entry is a class file
            if (entry.name.endsWith(".class")) {
                // Read class through ASM
                readClass(bytes, classes, classMapper)
            }

            // Copy it to temporary file
            output.putNextEntry(JarEntry(entry.name))
            output.write(bytes)
        }
    }

    private fun readClass(
        bytes: ByteArray,
        classes: MutableList<ClassInfo>,
        classMapper: MutableMap<ClassType, ClassInfo>) {
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()

        classReader.accept(
            classNode,
            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )
        val classInfo = ClassInfo(classNode.name, classNode)
        // Cache the class
        classes += classInfo

        // The class has been marked with @FairyInternalIdentityMeta
        if (hasInternalMetadata(classInfo)) {
            // Try mapping it to exist class type
            pushClassToMapping(classInfo, classMapper)
        }
    }

    private fun pushClassToMapping(classInfo: ClassInfo, classMapper: MutableMap<ClassType, ClassInfo>) {
        ClassType.values().forEach { classType ->
            if (!classInfo.name.contains("module-info") && classType.names.contains(classInfo.name.substringAfterLast("/"))) {
                // Duplicated class types
                if (classMapper.contains(classType))
                    throw IllegalStateException("a project are not suppose to have 2 or more classes that are $classType")
                classMapper[classType] = classInfo
            }
        }
    }

    private fun shouldExcludeFile(jarEntry: JarEntry): Boolean {
        if (jarEntry.name.equals("module.json")) return true
        if (jarEntry.name.equals("plugin.yml")) return true

        return false
    }

    private fun hasDependency(project: Project, name: String): Boolean {
        // hacky? I don't know
        return project.configurations.any { configuration -> configuration.dependencies.any { dependency -> dependency.name.equals(name) } }
    }

    private fun hasInternalMetadata(classInfo: ClassInfo): Boolean =
        classInfo.classNode.visibleAnnotations?.any { annotation -> annotation.desc.contains(ClassConstants.INTERNAL_META) } ?: false

    private fun hasFairyLaunch(classInfo: ClassInfo): Boolean =
        classInfo.classNode.visibleAnnotations?.any { annotation -> annotation.desc.contains(ClassConstants.FAIRY_LAUNCH) } ?: false
}

/**
 * Class type.
 */
enum class ClassType(vararg val names: String) {
    MAIN_CLASS, MAIN_CLASS_INTERFACE("Plugin", "Application"), BUKKIT_PLUGIN("BukkitPlugin");
}

/**
 * Class information.
 */
data class ClassInfo(val name: String, val classNode: ClassNode)