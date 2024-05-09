plugins {
    id("io.fairyproject.bootstrap")
}

dependencies {
    implementation(project(":core-bootstrap"))
    compileOnly("io.fairyproject:app-platform")

    testImplementation("io.fairyproject:app-tests")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.fairyproject.bootstrap.app.AppMain"
        attributes["Multi-Release"] = "true"
    }
}