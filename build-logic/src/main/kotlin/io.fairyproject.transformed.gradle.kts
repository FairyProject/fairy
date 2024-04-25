import io.fairyproject.gradle.ProjectTransformPlugin

plugins {
    id("io.fairyproject.common")
}

ProjectTransformPlugin().apply(project)

tasks {
    withType(JavaCompile::class.java).configureEach {
        options.compilerArgs.add("-parameters")
    }
}