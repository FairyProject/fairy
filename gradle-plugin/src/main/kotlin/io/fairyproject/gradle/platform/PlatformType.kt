package io.fairyproject.gradle.platform

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