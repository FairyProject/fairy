
plugins {
    id("io.fairyproject.versioned")
}

configurations {
    compileOnly {
        isCanBeResolved = true
    }
}

//sourceSets {
//    test.get().compileClasspath += configurations.compileOnly.get()
//    test.get().runtimeClasspath += configurations.compileOnly.get()
//}