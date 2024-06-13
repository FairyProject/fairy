import org.gradle.kotlin.dsl.`java-library`

plugins {
    `java-library`
}

group = "io.fairyproject"

repositories {
    mavenLocal() // development
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven ("https://repo.imanity.dev/imanity-libraries/")
    maven ("https://nexus.funkemunky.cc/content/repositories/releases/")
    maven ("https://jitpack.io")
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        content {
            includeGroup("org.spigotmc")
            includeGroup("net.md-5")
        }
    }
    maven {
        url = uri("https://repo.viaversion.com/")
        content {
            includeGroup("com.viaversion")
        }
    }
    // World Edit repository
    maven {
        url = uri("https://maven.enginehub.org/repo/")
        content {
            includeGroup("com.sk89q.worldedit")
            includeGroup("com.sk89q.lib")
            includeGroup("com.sk89q")
        }
    }
    // PaperMC repository
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
        content {
            includeGroup("com.destroystokyo.paper")
            includeGroup("io.papermc.paper")
            includeGroup("net.md-5")
        }
    }
    // PacketEvents CodeMC repository
    maven {
        url = uri("https://repo.codemc.io/repository/maven-releases/")
        content {
            includeGroup("com.github.retrooper")
        }
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        content {
            includeGroup("com.github.retrooper")
        }
    }
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