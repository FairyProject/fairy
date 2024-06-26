
plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    api("io.fairyproject:bukkit-platform")
    api("io.fairyproject:bukkit-command")
    api(project(":core-devtools"))
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
        exclude(group = "net.kyori")
    }

    testImplementation("dev.imanity.mockbukkit:MockBukkit1.16:1.0.1")
    testImplementation("io.fairyproject:bukkit-tests")
}