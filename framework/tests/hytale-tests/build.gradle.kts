plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    compileOnly("io.fairyproject:hytale-platform")
    api(project(":core-tests"))
}
