plugins {
    id("io.fairyproject.platform")
}

dependencies {
    api(project(":core-platform"))

    implementation("org.tinylog:tinylog-api:2.4.1")
    implementation("org.tinylog:tinylog-impl:2.4.1")
}