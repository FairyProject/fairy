plugins {
    `java-library`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.codehaus.groovy:groovy-all:3.0.9")

    implementation("org.yaml:snakeyaml:1.29")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly("org.jetbrains:annotations:23.0.0")
    annotationProcessor("org.jetbrains:annotations:23.0.0")

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:32.0.0-android")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
}