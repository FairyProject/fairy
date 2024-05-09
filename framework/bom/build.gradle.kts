plugins {
    id("io.fairyproject.bom")
}

description = "Fairy BOM (Bill of materials)"

dependencies {
    api(platform("io.fairyproject:bootstrap-bom"))
    api(platform("io.fairyproject:bundles-bom"))
    api(platform("io.fairyproject:devtools-bom"))
    api(platform("io.fairyproject:modules-bom"))
    api(platform("io.fairyproject:platforms-bom"))
    api(platform("io.fairyproject:tests-bom"))
}