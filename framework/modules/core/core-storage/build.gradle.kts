plugins {
    id("io.fairyproject.module")
}

dependencies {
    api("io.fairyproject:core-config")

    api("com.zaxxer:HikariCP:3.4.5")
    api("org.mongodb:bson:4.2.3")
    api("org.mongodb:mongodb-driver-core:4.2.3")
    api("org.mongodb:mongodb-driver-sync:4.2.3")
    api("org.mongojack:mongojack:4.2.0")
    api("net.bytebuddy:byte-buddy:1.10.9")
    api("com.h2database:h2:1.4.199")
}