package composite

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

val TaskContainer.publishTask: Task
    get() = this.findByName("publish") ?: this.register("publish").get()