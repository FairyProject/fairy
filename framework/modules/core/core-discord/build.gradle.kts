plugins {
    id("io.fairyproject.module")
}

repositories {
    mavenCentral() // for transitive dependencies
    maven("https://m2.dv8tion.net/releases/")
}

dependencies {
    implementation(project(":core-command"))
    implementation("net.dv8tion:JDA:4.3.0_339")
}
