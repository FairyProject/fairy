pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("../../../build-logic")
includeBuild("../../tests")
includeBuild("../../platforms")
includeBuild("..")

include(":core-bom")
include(":core-command")
include(":core-config")
include(":core-discord")
include(":core-storage")
