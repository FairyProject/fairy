plugins {
    `java`
    id("io.fairyproject")
    id("com.gradleup.shadow") version "9.3.1"
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

runHytaleServer {  }