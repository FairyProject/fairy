plugins {
    id("io.fairyproject.platform")
}

dependencies {
    api(project(":mc-platform"))
    api("net.kyori:adventure-platform-bukkit:4.3.3")
    api("net.kyori:adventure-text-serializer-bungeecord:4.3.3")
    api("com.github.retrooper.packetevents:spigot:2.3.0") {
        exclude(group = "net.kyori")
    }
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
        exclude(group = "net.kyori")
    }
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("io.netty:netty-all:4.1.100.Final")
    compileOnly("com.viaversion:viaversion:4.0.1")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:6.1.5")
    compileOnly("com.sk89q.worldedit:worldedit-core:6.0.0-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }

    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("io.fairyproject:bukkit-tests")
}