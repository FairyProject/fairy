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

    /**
     * Add support to folia.
     */
    var foliaSupported: Boolean
        get() = this["folia-supported"] as Boolean
        set(value) { this["folia-supported"] = value }

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

    val libraries: MutableList<String> by lazy {
        val list = mutableListOf<String>()
        this["libraries"] = list
        list
    }

}

/**
 * Author information for Hytale plugin.
 */
data class HytaleAuthor(
    var name: String = "",
    var email: String = "",
    var url: String = ""
)

/**
 * The properties of a Hytale plugin.
 */
class HytaleProperties : Properties(PlatformType.HYTALE) {

    var group: String
        get() = this["Group"] as? String ?: ""
        set(value) { this["Group"] = value }

    var website: String
        get() = this["Website"] as? String ?: ""
        set(value) { this["Website"] = value }

    var serverVersion: String
        get() = this["ServerVersion"] as? String ?: ""
        set(value) { this["ServerVersion"] = value }

    var disabledByDefault: Boolean
        get() = this["DisabledByDefault"] as? Boolean ?: false
        set(value) { this["DisabledByDefault"] = value }

    var includesAssetPack: Boolean
        get() = this["IncludesAssetPack"] as? Boolean ?: true
        set(value) { this["IncludesAssetPack"] = value }

    val authors: MutableList<HytaleAuthor> by lazy {
        val list = mutableListOf<HytaleAuthor>()
        this["Authors"] = list
        list
    }

    val dependencies: MutableMap<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        this["Dependencies"] = map
        map
    }

    val optionalDependencies: MutableMap<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        this["OptionalDependencies"] = map
        map
    }

    val loadBefore: MutableMap<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        this["LoadBefore"] = map
        map
    }

    val subPlugins: MutableList<String> by lazy {
        val list = mutableListOf<String>()
        this["SubPlugins"] = list
        list
    }

}