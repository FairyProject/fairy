plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    val platform = project.name.split("-")[0]

    compileOnly("io.fairyproject:$platform-platform")
    testImplementation("io.fairyproject:$platform-platform")
    testImplementation("io.fairyproject:$platform-tests")
}