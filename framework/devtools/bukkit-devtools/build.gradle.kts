plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    api("io.fairyproject:bukkit-platform")
    api("io.fairyproject:bukkit-command")
    api(project(":core-devtools"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")

    testImplementation("dev.imanity.mockbukkit:MockBukkit1.16:1.0.1")
    testImplementation("io.fairyproject:bukkit-tests")
}