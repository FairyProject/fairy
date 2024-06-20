plugins {
    id("io.fairyproject.versioned")
    id("io.fairyproject.publish")
}

dependencies {
    compileOnly("io.fairyproject:bukkit-platform")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
        exclude(group = "net.kyori")
    }

    api(project(":mc-tests"))
    compileOnly("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")
}