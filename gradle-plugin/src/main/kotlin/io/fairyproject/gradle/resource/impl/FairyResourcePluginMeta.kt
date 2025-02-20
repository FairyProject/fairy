package io.fairyproject.gradle.resource.impl

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.fairyproject.gradle.resource.*

/**
 * The resource generator for plugin meta.
 */
open class FairyResourcePluginMeta : FairyResource {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun generate(
        context: FairyResourceGenerateContext,
        classMapper: Map<ClassType, ClassInfo>
    ): ResourceInfo {
        val jsonObject = JsonObject()

        jsonObject.addProperty("name", context.pluginName)
        val classInfo = classMapper[ClassType.MAIN_CLASS]
        if (classInfo != null) {
            jsonObject.addProperty("mainClass", classInfo.name.replace('/', '.'))
        }
        context.mainPackage?.let {
            jsonObject.addProperty("shadedPackage", it)
        }
        context.fairyPackage?.let {
            jsonObject.addProperty("fairyPackage", it)
        }

        return resourceOf("fairy.json", gson.toJson(jsonObject).encodeToByteArray())
    }
}