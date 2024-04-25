plugins {
    id("io.fairyproject.bootstrap")
}

dependencies {
    compileOnly("io.fairyproject:core-platform")

    testImplementation("io.fairyproject:core-tests")
}