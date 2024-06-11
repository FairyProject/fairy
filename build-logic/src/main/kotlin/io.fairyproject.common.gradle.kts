import org.gradle.kotlin.dsl.`java-library`

plugins {
    `java-library`
}

group = "io.fairyproject"

repositories {
    mavenLocal() // development
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven ("https://repo.imanity.dev/imanity-libraries/")
    maven ("https://nexus.funkemunky.cc/content/repositories/releases/")
    maven ("https://jitpack.io")
    maven ("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven ("https://repo.viaversion.com/")
    maven ("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testCompileOnly("org.jetbrains:annotations:24.1.0")
    testImplementation("org.mockito:mockito-core:4.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

java {
    disableAutoTargetJvm()
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
    options.release = 8
}