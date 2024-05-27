plugins {
    `java`
    id("io.fairyproject")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    mavenLocal()
}

fairy {
    name.set("test-plugin")
    mainPackage.set("io.example.debug")
    fairyPackage.set("io.fairyproject")
}

dependencies {
    implementation("io.fairyproject:bukkit-bundles")
    implementation("io.fairyproject:mc-sidebar")
    implementation("io.fairyproject:mc-tablist")
}

runServer {
    version = "1.20.6"
    javaVersion.set(JavaVersion.VERSION_21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}