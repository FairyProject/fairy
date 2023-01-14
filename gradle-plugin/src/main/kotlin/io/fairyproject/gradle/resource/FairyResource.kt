package io.fairyproject.gradle.resource

import io.fairyproject.gradle.extension.FairyExtension
import io.fairyproject.gradle.resource.impl.FairyResourceBukkitMeta
import io.fairyproject.gradle.resource.impl.FairyResourcePluginMeta
import org.gradle.api.Project

interface FairyResource {

    fun generate(
        project: Project,
        fairyExtension: FairyExtension,
        classMapper: Map<ClassType, ClassInfo>
    ): ResourceInfo?

    companion object {

        val ALL = arrayOf(
            FairyResourcePluginMeta(),
            FairyResourceBukkitMeta()
        )

    }

}

fun resourceOf(name: String, byteArray: ByteArray): ResourceInfo = ResourceInfo(name, byteArray)

data class ResourceInfo(val name: String, val byteArray: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceInfo

        if (name != other.name) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}