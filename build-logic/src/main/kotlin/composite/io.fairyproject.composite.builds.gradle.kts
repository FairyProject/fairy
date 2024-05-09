import composite.publishTask

tasks.publishTask.run {
    gradle.includedBuilds.forEach {
        if (it.name != "build-logic" && it.name != "shared")
            this.dependsOn(it.task(":publish"))
    }
}