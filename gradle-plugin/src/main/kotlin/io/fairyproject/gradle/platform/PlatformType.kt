package io.fairyproject.gradle.platform

/**
 * The platform type.
 */
enum class PlatformType {
    BUKKIT,
    APP,
    CORE;

    val dependencyName: String
        get() = name.lowercase()

    companion object {
        val VALUES = values().toList()
    }
}