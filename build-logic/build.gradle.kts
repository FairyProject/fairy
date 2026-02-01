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

    implementation("org.yaml:snakeyaml:2.0")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly("org.jetbrains:annotations:23.0.0")
    annotationProcessor("org.jetbrains:annotations:23.0.0")

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:32.0.1-android")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.ow2.asm:asm:9.9.1")
    implementation("org.ow2.asm:asm-commons:9.9.1")
}