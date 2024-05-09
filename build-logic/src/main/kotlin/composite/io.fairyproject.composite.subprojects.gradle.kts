import composite.publishTask

subprojects.forEach {
    it.plugins.whenPluginAdded(CompositeSubprojectsAction(it))
}

/**
 * This class is used to add a dependency to the publishing task of the subprojects.
 */
class CompositeSubprojectsAction(private val project: Project): Action<Plugin<*>> {
    override fun execute(t: Plugin<*>) {
        if (t is MavenPublishPlugin)
            tasks.publishTask.dependsOn(project.tasks.getByName("publish"))
    }
}