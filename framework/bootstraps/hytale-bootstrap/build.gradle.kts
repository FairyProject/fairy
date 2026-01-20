plugins {
    id("io.fairyproject.bootstrap")
}

dependencies {
    api(project(":core-bootstrap"))
    compileOnly("io.fairyproject:hytale-platform")
}