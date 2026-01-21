package io.fairyproject.gradle.resource.impl

import com.google.gson.GsonBuilder
import io.fairyproject.gradle.extension.property.HytaleAuthor
import io.fairyproject.gradle.resource.*

/**
 * The resource generator for Hytale manifest.json
 */
class FairyResourceHytaleMeta : FairyResource {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun generate(
        context: FairyResourceGenerateContext,
        classMapper: Map<ClassType, ClassInfo>
    ): ResourceInfo? {
        if (!context.hasHytalePlatform)
            return null

        classMapper[ClassType.HYTALE_PLUGIN] ?: return null

        val manifest = mutableMapOf<String, Any>()

        // Set Main class - this is the HytalePlugin class that extends Hytale's JavaPlugin
        val hytalePluginClass = classMapper[ClassType.HYTALE_PLUGIN]!!
        manifest["Main"] = hytalePluginClass.name.replace("/", ".")

        // Set Group from hytaleProps or default
        val group = context.hytaleProps["Group"] as? String
        manifest["Group"] = if (group.isNullOrEmpty()) "io.fairyproject" else group

        // Set Name from context
        manifest["Name"] = context.pluginName

        // Set Version from context - must be valid semver format
        manifest["Version"] = convertToValidSemver(context.projectVersion)

        // Set Description from context
        manifest["Description"] = context.projectDescription

        // Set Authors
        val authors = context.hytaleProps["Authors"]
        manifest["Authors"] = if (authors is List<*> && authors.isNotEmpty()) {
            authors.map { author ->
                when (author) {
                    is HytaleAuthor -> mapOf(
                        "Name" to author.name,
                        "Email" to author.email,
                        "Url" to author.url
                    )
                    else -> mapOf("Name" to "", "Email" to "", "Url" to "")
                }
            }
        } else {
            emptyList<Map<String, String>>()
        }

        // Set Website
        manifest["Website"] = context.hytaleProps["Website"] as? String ?: ""

        // Set ServerVersion
        manifest["ServerVersion"] = context.hytaleProps["ServerVersion"] as? String ?: ""

        // Set Dependencies
        manifest["Dependencies"] = context.hytaleProps["Dependencies"] as? Map<*, *> ?: emptyMap<String, String>()

        // Set OptionalDependencies
        manifest["OptionalDependencies"] = context.hytaleProps["OptionalDependencies"] as? Map<*, *> ?: emptyMap<String, String>()

        // Set LoadBefore
        manifest["LoadBefore"] = context.hytaleProps["LoadBefore"] as? Map<*, *> ?: emptyMap<String, String>()

        // Set DisabledByDefault
        manifest["DisabledByDefault"] = context.hytaleProps["DisabledByDefault"] as? Boolean ?: false

        // Set IncludesAssetPack
        manifest["IncludesAssetPack"] = context.hytaleProps["IncludesAssetPack"] as? Boolean ?: true

        // Set SubPlugins
        manifest["SubPlugins"] = context.hytaleProps["SubPlugins"] as? List<*> ?: emptyList<String>()

        val json = gson.toJson(manifest)
        return resourceOf("manifest.json", json.encodeToByteArray())
    }

    private fun convertToValidSemver(version: String): String {
        if (version.isBlank() || version == "unspecified") {
            return "0.0.1"
        }

        val semverRegex = Regex("""^\d+\.\d+\.\d+(-[\w.]+)?(\+[\w.]+)?$""")
        if (semverRegex.matches(version)) {
            return version
        }

        val numbers = Regex("""\d+""").findAll(version).map { it.value }.toList()
        return when {
            numbers.size >= 3 -> "${numbers[0]}.${numbers[1]}.${numbers[2]}"
            numbers.size == 2 -> "${numbers[0]}.${numbers[1]}.0"
            numbers.size == 1 -> "${numbers[0]}.0.0"
            else -> "0.0.1"
        }
    }

}
