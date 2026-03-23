plugins {
    id("io.fairyproject.module")
}

dependencies {
    compileOnly("dev.imanity.hytale:HytaleServer:2026.01.17-2")
}

repositories {
    maven ("https://repo.imanity.dev/imanity-libraries/")
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
