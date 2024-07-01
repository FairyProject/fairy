plugins {
    id("io.fairyproject.module")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
        exclude(group = "net.kyori")
    }
    testImplementation("dev.imanity.mockbukkit:MockBukkit1.16:1.0.17")
}