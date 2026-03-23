package io.fairyproject.gradle.resource

import io.fairyproject.gradle.resource.impl.FairyResourceBukkitMeta
import io.fairyproject.gradle.resource.impl.FairyResourceHytaleMeta
import io.fairyproject.gradle.resource.impl.FairyResourcePluginMeta

/**
 * The resource generator.
 */
interface FairyResource {

    /**
     * Generate the resource.
     */
    fun generate(
        context: FairyResourceGenerateContext,
        classMapper: Map<ClassType, ClassInfo>
    ): ResourceInfo?

    companion object {

        val ALL = arrayOf(
            FairyResourcePluginMeta(),
            FairyResourceBukkitMeta(),
            FairyResourceHytaleMeta()
        )

    }

}

/**
 * The context for generating the resource.
 */
data class FairyResourceGenerateContext(
    val projectName: String,
    val projectVersion: String,
    val projectDescription: String,
    val hasBukkitPlatform: Boolean,
    val hasHytalePlatform: Boolean,
    private val _pluginName: String?,
    val mainPackage: String?,
    val fairyPackage: String?,
    val props: Map<String, Any>,
    val hytaleProps: Map<String, Any>
) {
    val pluginName: String
        get() = _pluginName ?: projectName
}

/**
 * Create a new [ResourceInfo] instance.
 */
fun resourceOf(name: String, byteArray: ByteArray): ResourceInfo = ResourceInfo(name, byteArray)

/**
 * The resource info.
 */
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