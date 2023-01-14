package io.fairyproject.gradle.resource

import io.fairyproject.gradle.constants.ClassConstants
import io.fairyproject.gradle.extension.FairyExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.BufferedOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

open class FairyResourceAction : Action<Task> {
    override fun execute(task: Task) {
        val jar = task as Jar
        val project = jar.project
        val extension = project.extensions.getByType(FairyExtension::class.java)
        val file = jar.archiveFile.get().asFile
        val outputFile = kotlin.io.path.createTempFile(file.nameWithoutExtension, file.extension).toFile()

        JarFile(file).use { inJar -> JarOutputStream(BufferedOutputStream(outputFile.outputStream())).use { output ->
            try {
                val classMapper = mutableMapOf<ClassType, ClassInfo>()
                val classes = mutableListOf<ClassInfo>()

                // Read input jar information and copy to temporary file
                for (entry in inJar.entries()) {
                    if (this.shouldExcludeFile(entry)) continue
                    val bytes = inJar.getInputStream(entry).readBytes()
                    // The entry is a class file
                    if (entry.name.endsWith(".class")) {
                        // Read class through ASM
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
                            ClassType.values().forEach { classType ->
                                if (classType.names.contains(classInfo.name.substringAfterLast("/"))) {
                                    // Duplicated class types
                                    if (classMapper.contains(classType))
                                        throw IllegalStateException("a project are not suppose to have 2 or more classes that are $classType")
                                    classMapper[classType] = classInfo
                                }
                            }
                        }
                    }

                    // Copy it to temporary file
                    output.putNextEntry(JarEntry(entry.name))
                    output.write(bytes)
                }


                // Second loop to identify main class
                val mainClassInterface = classMapper[ClassType.MAIN_CLASS_INTERFACE]
                mainClassInterface ?: return // maybe it's not shaded.

                classes.forEach {
                    if (it.classNode.superName == mainClassInterface.name) {
                        // the super class was main class interface, so it's main class
                        classMapper[ClassType.MAIN_CLASS] = it
                    }
                }

                // Generate resource
                FairyResource.ALL.forEach {
                    it.generate(project, extension, classMapper)?.let { resource ->
                        output.putNextEntry(JarEntry(resource.name))
                        output.write(resource.byteArray)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }}

        // Write temporary file back to original file
        outputFile.copyTo(file, true)
        outputFile.delete()
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
}

enum class ClassType(vararg val names: String) {
    MAIN_CLASS, MAIN_CLASS_INTERFACE("Plugin", "Application"), BUKKIT_PLUGIN("BukkitPlugin");
}

data class ClassInfo(val name: String, val classNode: ClassNode)