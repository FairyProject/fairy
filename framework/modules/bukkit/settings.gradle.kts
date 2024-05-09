pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../../build-logic")
includeBuild("../../tests")
includeBuild("../../platforms")
includeBuild("..")

include(":bukkit-bom")
include(":bukkit-command")
include(":bukkit-locale")
include(":bukkit-map")
include(":bukkit-storage")
include(":bukkit-gui")
include(":bukkit-items")
include(":bukkit-menu")
include(":bukkit-nbt")
include(":bukkit-timer")
include(":bukkit-visibility")
include(":bukkit-visual")
include(":bukkit-xseries")
