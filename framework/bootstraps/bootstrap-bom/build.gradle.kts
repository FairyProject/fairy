plugins {
    id("io.fairyproject.bom")
}

description = "Fairy Bootstrap BOM (Bill of materials)"

dependencies {
    constraints {
        rootProject.subprojects.forEach {
            if (it != project)
                api(project(it.path))
        }
    }
}