import composite.cleanTask
import composite.publishTask
import composite.testTask

subprojects.forEach { p ->
    p.tasks.findByName("test")?.let {
        tasks.testTask.dependsOn(it)
    }

    p.tasks.findByName("publish")?.let {
        tasks.publishTask.dependsOn(it)
    }

    p.tasks.findByName("clean")?.let {
        tasks.cleanTask.dependsOn(it)
    }

    p.tasks.whenTaskAdded {
        if (name == "test")
            tasks.testTask.dependsOn(this)

        if (name == "publish")
            tasks.publishTask.dependsOn(this)

        if (name == "clean")
            tasks.cleanTask.dependsOn(this)
    }
}