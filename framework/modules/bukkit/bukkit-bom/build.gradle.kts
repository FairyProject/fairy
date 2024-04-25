plugins {
    id("io.fairyproject.bom")
}

description = "Fairy Bukkit Modules BOM (Bill of materials)"

dependencies {
    constraints {
        rootProject.subprojects.forEach {
            if (it != project)
                api(project(it.path))
        }
    }
}