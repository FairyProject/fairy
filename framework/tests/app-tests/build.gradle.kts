plugins {
    id("io.fairyproject.versioned")
}

dependencies {
    compileOnlyApi("io.fairyproject:app-platform")

    api(project(":core-tests"))
}