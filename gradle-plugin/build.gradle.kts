import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.gradle.plugin-publish") version "1.0.0"
    id("io.fairyproject.common")
    id("io.fairyproject.publish")
    `java-gradle-plugin`
}

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

/**
 * Get global property from global.properties file
 *
 * @param key The key of the property
 * @return The value of the property
 */
fun Project.getGlobalProperty(key: String): String {
    return this.globalProperties[key].toString()
}

version = getGlobalProperty("version")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.spring.gradle:dependency-management-plugin:1.1.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.json:json:20231013")
    implementation("org.apache.maven:maven-plugin-api:3.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.7.22")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("com.google.code.gson:gson:2.10")
    implementation("io.github.toolfactory:narcissus:1.0.7")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
}

gradlePlugin {
    plugins {
        create("fairy") {
            displayName = "Fairy plugin"
            description = "A Gradle plugin that provides ability to manage fairy project easily."
            id = "io.fairyproject"
            implementationClass = "io.fairyproject.gradle.FairyGradlePlugin"
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}