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
    name.set("test-hytale-plugin")
    mainPackage.set("io.example.hytale")
    fairyPackage.set("io.fairyproject")
}

dependencies {
    implementation("io.fairyproject:hytale-bootstrap")
    implementation("io.fairyproject:hytale-platform")
    implementation("io.fairyproject:hytale-command")
    implementation("io.fairyproject:core-devtools")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

runHytaleServer {  }