plugins {
    id("io.fairyproject.bootstrap")
}

dependencies {
    api(project(":core-bootstrap"))
    compileOnly("io.fairyproject:hytale-platform")
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