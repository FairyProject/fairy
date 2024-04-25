plugins {
    id("io.fairyproject.versioned")
}

dependencies {
    compileOnly("io.fairyproject:bukkit-platform")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    api(project(":mc-tests"))
    compileOnly("dev.imanity.mockbukkit:MockBukkit1.16:1.0.1")
}