plugins {
    id("io.fairyproject.module.bukkit")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")

    api("io.fairyproject:core-command")
    api("com.github.cryptomorin:XSeries:9.10.0")

    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

tasks {
    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "8"
    }
}