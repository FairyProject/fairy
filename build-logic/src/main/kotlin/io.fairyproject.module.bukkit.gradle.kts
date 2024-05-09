plugins {
    id("io.fairyproject.module")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    testImplementation("dev.imanity.mockbukkit:MockBukkit1.16:1.0.17")
}