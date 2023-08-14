package io.fairyproject.gradle.extension.property

import io.fairyproject.gradle.platform.PlatformType

/**
 * The properties of a plugin.
 */
sealed class Properties(val platformType: PlatformType) : HashMap<String, Any>()

/**
 * The properties of a bukkit plugin.
 */
class BukkitProperties : Properties(PlatformType.BUKKIT) {

    // TODO - add all bukkit properties

    var bukkitApi: String
        get() = this["api-version"] as String
        set(value) { this["api-version"] = value }

    var description: String
        get() = this["description"] as String
        set(value) { this["description"] = value }

    var website: String
        get() = this["website"] as String
        set(value) { this["website"] = value }

    val authors: MutableList<String> by lazy {
        val list = mutableListOf<String>()
        this["authors"] = list
        list
    }

    val depends: MutableList<String> by lazy {
        val list = mutableListOf<String>()
        this["depend"] = list
        list
    }

    val softDepends: MutableList<String> by lazy {
        val list = mutableListOf<String>()
        this["softdepend"] = list
        list
    }

    val loadBefore: MutableList<String> by lazy {
        val list = mutableListOf<String>()
        this["loadbefore"] = list
        list
    }

}