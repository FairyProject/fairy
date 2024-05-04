val publishTask = tasks.findByName("publish") ?: tasks.register("publish").get()
publishTask.run {
    gradle.includedBuilds.forEach {
        if (it.name == "build-logic" || it.name == "shared")
            return@forEach

        this.dependsOn(it.task(":publish"))
    }
}