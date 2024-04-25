pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../../build-logic")
includeBuild("../../tests")
includeBuild("../../platforms")
includeBuild("..")

include(":mc-bom")
include(":mc-locale")
include(":mc-actionbar")
include(":mc-animation")
include(":mc-hologram")
include(":mc-map")
include(":mc-nametag")
include(":mc-sidebar")
include(":mc-tablist")
