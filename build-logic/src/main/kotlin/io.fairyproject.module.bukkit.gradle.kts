plugins {
    id("io.fairyproject.module")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    testImplementation("dev.imanity.mockbukkit:MockBukkit1.16:1.0.17")
}