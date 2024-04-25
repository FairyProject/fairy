plugins {
    id("io.fairyproject.bom")
}

description = "Fairy DevTools BOM (Bill of materials)"

dependencies {
    api(platform("io.fairyproject:core-bom"))
    api(platform("io.fairyproject:bukkit-bom"))
    api(platform("io.fairyproject:mc-bom"))
}