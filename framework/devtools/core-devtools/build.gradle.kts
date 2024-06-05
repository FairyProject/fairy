plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    compileOnlyApi("io.fairyproject:core-platform")
    api("io.fairyproject:core-config")
    implementation("commons-io:commons-io:2.14.0")

    testImplementation("io.fairyproject:core-tests")
}