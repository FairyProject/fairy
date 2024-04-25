plugins {
    id("io.fairyproject.versioned")
}

dependencies {
    compileOnly("io.fairyproject:mc-platform")

    api(project(":core-tests"))
    api("io.netty:netty-all:4.1.100.Final")
}