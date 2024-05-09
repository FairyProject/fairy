pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../build-logic")
includeBuild("../platforms")

include(":core-tests")
include(":app-tests")
include(":mc-tests")
include(":bukkit-tests")
include(":tests-bom")
