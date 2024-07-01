package composite

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

val TaskContainer.publishTask: Task
    get() = this.findByName("publish") ?: this.register("publish").get()

val TaskContainer.cleanTask: Task
    get() = this.findByName("clean") ?: this.register("clean").get()

val TaskContainer.testTask: Task
    get() = this.findByName("test") ?: this.register("test").get()
