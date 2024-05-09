pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("core")
includeBuild("bukkit")
includeBuild("mc")

include(":modules-bom")