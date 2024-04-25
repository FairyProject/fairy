package util

import org.gradle.api.Project
import java.util.*

val Project.globalProperties: Properties
    get() =
        Properties().apply {
            var path = project.file("..")
            // Resolve global.properties file recursively
            while (!path.resolve("global.properties").exists()) {
                path = path.parentFile
            }
            load(path.resolve("global.properties").inputStream())
        }


fun Project.getGlobalProperty(key: String): String {
    return this.globalProperties[key].toString()
}