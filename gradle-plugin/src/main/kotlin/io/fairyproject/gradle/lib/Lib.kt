package io.fairyproject.gradle.lib

import com.google.gson.JsonObject

/**
 * A library document.
 */
data class Lib(val dependency: String, val repository: String?) {
    val jsonObject: JsonObject by lazy {
        val jsonObject = JsonObject()
        jsonObject.addProperty("dependency", dependency)
        repository ?.run {
            jsonObject.addProperty("repository", repository)
        }
        jsonObject
    }
}