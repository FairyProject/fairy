pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("core")
includeBuild("bukkit")
includeBuild("mc")
includeBuild("hytale")

include(":modules-bom")