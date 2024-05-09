plugins {
    id("io.fairyproject.versioned")
}

dependencies {
    compileOnlyApi("io.fairyproject:core-platform")
    api("io.fairyproject:core-config")
    implementation("commons-io:commons-io:2.14.0")

    testImplementation("io.fairyproject:core-tests")
}