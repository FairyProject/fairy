package io.fairyproject.gradle.extension

import io.fairyproject.gradle.extension.property.BukkitProperties
import io.fairyproject.gradle.extension.property.Properties
import io.fairyproject.gradle.platform.PlatformType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class FairyExtension(objectFactory: ObjectFactory) {

    val version: Property<String> = objectFactory.property(String::class.java)
    val mainPackage: Property<String> = objectFactory.property(String::class.java)

    private val properties = mutableMapOf<PlatformType, Properties>()

    /**
     * Set the fairy version
     */
    fun version(version: String) {
        this.version.set(version)
    }

    /**
     * Get properties for bukkit platform
     */
    fun bukkitProperties(): BukkitProperties = this.properties.computeIfAbsent(PlatformType.BUKKIT) { BukkitProperties() } as BukkitProperties

}