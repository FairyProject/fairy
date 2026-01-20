plugins {
    id("io.fairyproject.platform")
}


dependencies {
    api(project(":core-platform"))
    compileOnlyApi("dev.imanity.hytale:HytaleServer:2026.01.17-2")
}
