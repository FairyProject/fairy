package io.fairyproject.gradle.extension

import io.fairyproject.gradle.extension.property.BukkitProperties
import io.fairyproject.gradle.extension.property.Properties
import io.fairyproject.gradle.platform.PlatformType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Fairy extension.
 */
open class FairyExtension(objectFactory: ObjectFactory) {

    val name: Property<String> = objectFactory.property(String::class.java)
    val mainPackage: Property<String> = objectFactory.property(String::class.java)
    val fairyPackage: Property<String> = objectFactory.property(String::class.java)
        .convention("io.fairyproject")

    private val properties = mutableMapOf<PlatformType, Properties>()

    /**
     * Get properties for bukkit platform
     */
    fun bukkitProperties(): BukkitProperties = this.properties.computeIfAbsent(PlatformType.BUKKIT) { BukkitProperties() } as BukkitProperties

}