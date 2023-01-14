import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.gradle.plugin-publish") version "1.0.0"
    `java-gradle-plugin`
    `maven-publish`
}

version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.spring.gradle:dependency-management-plugin:1.1.0")
    implementation("org.json:json:20220924")
    implementation("org.apache.maven:maven-plugin-api:3.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.7.22")
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-commons:9.4")
    implementation("com.google.code.gson:gson:2.10")
    implementation("io.github.toolfactory:narcissus:1.0.7")
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

pluginBundle {
    tags = listOf("fairy", "bukkit", "minecraft")
    website = "https://github.com/FairyProject/fairy"
    vcsUrl = "https://github.com/FairyProject/fairy"
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
        )
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}