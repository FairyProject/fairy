plugins {
    id("io.fairyproject.module")
}

dependencies {
    compileOnly("dev.imanity.hytale:HytaleServer:2026.01.17-2")
}

repositories {
    maven ("https://repo.imanity.dev/imanity-libraries/")
}