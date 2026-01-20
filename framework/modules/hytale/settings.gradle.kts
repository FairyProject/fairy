pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../../build-logic")
includeBuild("../../tests")
includeBuild("../../platforms")
includeBuild("..")

include(":hytale-bom")
include(":hytale-command")
