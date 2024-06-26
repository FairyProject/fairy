plugins {
    id("io.fairyproject.bootstrap")
}

dependencies {
    api(project(":core-bootstrap"))
    compileOnly("io.fairyproject:bukkit-platform")

    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
        exclude(group = "net.kyori")
    }
    testImplementation("io.fairyproject:bukkit-platform")
    testImplementation("dev.imanity.mockbukkit:MockBukkit1.16:1.0.1")
    testImplementation("io.fairyproject:bukkit-tests")
}