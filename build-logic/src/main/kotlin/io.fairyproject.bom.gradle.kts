import org.gradle.kotlin.dsl.`java-platform`
import util.getGlobalProperty

plugins {
    `java-platform`
    id("io.fairyproject.publish")
}

group = "io.fairyproject"
version = getGlobalProperty("version")

javaPlatform {
    allowDependencies()
}