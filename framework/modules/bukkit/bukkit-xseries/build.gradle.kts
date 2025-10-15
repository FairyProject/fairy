plugins {
    id("io.fairyproject.module")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.3-R0.1-SNAPSHOT")

    api("io.fairyproject:core-command")
    api("com.github.cryptomorin:XSeries:13.5.0")

    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

tasks {
    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
