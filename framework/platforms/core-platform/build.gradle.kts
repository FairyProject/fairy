plugins {
    id("io.fairyproject.platform")
}

dependencies {
    api("io.github.classgraph:classgraph:4.8.173")
    api("org.burningwave:jvm-driver:8.16.1")
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("javax.persistence:javax.persistence-api:2.2")
    api("com.google.code.gson:gson:2.9.1")
    api("org.yaml:snakeyaml:2.2")

    testImplementation("io.fairyproject:core-tests")
    testImplementation("javax.el:javax.el-api:3.0.1-b04")
}