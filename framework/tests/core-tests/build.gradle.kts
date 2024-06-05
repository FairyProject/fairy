plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    compileOnly("io.fairyproject:core-platform")

    api("org.junit.jupiter:junit-jupiter:5.9.0")
    api("org.mockito:mockito-core:4.11.0")
}