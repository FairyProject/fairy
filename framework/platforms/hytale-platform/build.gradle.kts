plugins {
    id("io.fairyproject.platform")
}

dependencies {
    api(project(":core-platform"))
    compileOnlyApi("dev.imanity.hytale:HytaleServer:2026.01.17-2")
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
