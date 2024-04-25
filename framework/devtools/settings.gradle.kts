pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../build-logic")
includeBuild("../platforms")
includeBuild("../tests")

include(":core-devtools")
include(":bukkit-devtools")
include(":devtools-bom")
