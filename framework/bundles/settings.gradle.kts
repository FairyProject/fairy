pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../build-logic")
includeBuild("../platforms")
includeBuild("../bootstraps")

include(":bukkit-bundles")
include(":bundles-bom")
