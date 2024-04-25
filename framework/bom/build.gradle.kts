plugins {
    id("io.fairyproject.bom")
}

description = "Fairy BOM (Bill of materials)"

dependencies {
    api("io.fairyproject:bootstrap-bom")
    api("io.fairyproject:bundles-bom")
    api("io.fairyproject:devtools-bom")
    api("io.fairyproject:modules-bom")
    api("io.fairyproject:platforms-bom")
    api("io.fairyproject:tests-bom")
}