pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

includeBuild("platforms")
includeBuild("bootstraps")
includeBuild("devtools")
includeBuild("bundles")
includeBuild("tests")
includeBuild("modules")

include(":bom")
