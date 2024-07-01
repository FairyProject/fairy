import composite.cleanTask
import composite.publishTask
import composite.testTask

tasks.publishTask.run {
    gradle.includedBuilds.forEach {
        if (it.name != "build-logic" && it.name != "shared")
            this.dependsOn(it.task(":publish"))
    }
}

tasks.testTask.run {
    gradle.includedBuilds.forEach {
        if (it.name != "build-logic" && it.name != "shared")
            this.dependsOn(it.task(":test"))
    }
}

tasks.cleanTask.run {
    gradle.includedBuilds.forEach {
        if (it.name != "build-logic" && it.name != "shared")
            this.dependsOn(it.task(":clean"))
    }
}