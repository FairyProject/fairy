plugins {
    id("io.fairyproject.bom")
}

description = "Fairy DevTools BOM (Bill of materials)"

dependencies {
    constraints {
        rootProject.subprojects.forEach {
            if (it != project)
                api(project(it.path))
        }
    }
}