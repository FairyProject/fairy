pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../build-logic")
includeBuild("../platforms")
includeBuild("../tests")

include(":core-bootstrap")
include(":bukkit-bootstrap")
include(":app-bootstrap")
include(":hytale-bootstrap")
include(":bootstrap-bom")