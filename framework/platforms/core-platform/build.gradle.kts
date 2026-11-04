plugins {
    id("io.fairyproject.platform")
    id("me.champeau.jmh") version "0.7.2"
}

dependencies {
    api("io.github.classgraph:classgraph:4.8.180")
    api("io.github.toolfactory:jvm-driver:9.9.10")
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("javax.persistence:javax.persistence-api:2.2")
    api("com.google.code.gson:gson:2.9.1")
    api("org.yaml:snakeyaml:2.2")

    testImplementation("io.fairyproject:core-tests")
    testImplementation("javax.el:javax.el-api:3.0.1-b04")
}

tasks.register<Test>("classLoaderCompatibilityTest") {
    description = "Tests URL class loader access on Java 25 with compact headers enabled or disabled."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    useJUnitPlatform()
    filter {
        includeTestsMatching("io.fairyproject.util.URLClassLoaderAccessTest")
    }
    val compactHeaders = providers.gradleProperty("compactObjectHeaders").getOrElse("true").toBoolean()
    jvmArgs("-XX:${if (compactHeaders) "+" else "-"}UseCompactObjectHeaders", "-XX:-CreateCoredumpOnCrash")
}
