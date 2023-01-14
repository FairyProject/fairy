package io.fairyproject.gradle.lib

import com.google.gson.JsonObject

data class Lib(val dependency: String, val repository: String?) {
    val jsonObject: JsonObject by lazy {
        val jsonObject = JsonObject()
        jsonObject.addProperty("dependency", dependency)
        repository ?.let { jsonObject.addProperty("repository", it) }
        jsonObject
    }
}

fun libOf(jsonObject: JsonObject): Lib {
    val dependency = jsonObject.get("dependency").asString
    var repository: String? = null
    if (jsonObject.has("repository"))
        repository = jsonObject.get("repository").asString
    return Lib(dependency, repository)
}