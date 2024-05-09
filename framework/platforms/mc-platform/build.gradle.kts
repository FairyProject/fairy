plugins {
    id("io.fairyproject.platform")
}

dependencies {
    api(project(":core-platform"))
    compileOnly("io.netty:netty-all:4.1.100.Final")

    // Adventure, for user-interface
    api("net.kyori:adventure-api:4.16.0")
    api("net.kyori:adventure-text-serializer-gson:4.16.0")
    api("net.kyori:adventure-text-serializer-legacy:4.16.0")
    api("net.kyori:adventure-nbt:4.16.0")
    api("net.kyori:adventure-text-minimessage:4.16.0")
    api("net.kyori:adventure-text-serializer-gson-legacy-impl:4.16.0")
    api("net.kyori:adventure-text-serializer-plain:4.16.0")

    api("com.github.retrooper.packetevents:api:2.3.0") {
        exclude(group = "net.kyori")
    }

    testImplementation("org.mockito:mockito-core:4.11.0")
}