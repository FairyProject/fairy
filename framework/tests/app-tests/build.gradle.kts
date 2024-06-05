plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    compileOnlyApi("io.fairyproject:app-platform")

    api(project(":core-tests"))
}