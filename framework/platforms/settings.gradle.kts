pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../build-logic")
includeBuild("../tests")

include(":core-platform")
include(":app-platform")
include(":mc-platform")
include(":bukkit-platform")
include(":hytale-platform")
include(":platforms-bom")
