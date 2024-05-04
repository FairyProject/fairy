val publishTask = tasks.findByName("publish") ?: tasks.register("publish").get()

subprojects.forEach {
    it.plugins.whenPluginAdded(CompositeSubprojectsAction(it))
}

class CompositeSubprojectsAction(private val project: Project): Action<Plugin<*>> {
    override fun execute(t: Plugin<*>) {
        if (t is MavenPublishPlugin)
            publishTask.dependsOn(project.tasks.getByName("publish"))
    }
}