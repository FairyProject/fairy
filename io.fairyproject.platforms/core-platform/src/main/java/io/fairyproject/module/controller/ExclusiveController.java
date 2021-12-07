package io.fairyproject.module.controller;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.Component;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.module.Module;
import io.fairyproject.module.ModuleController;
import io.fairyproject.module.ModuleService;
import io.fairyproject.util.exceptionally.ThrowingRunnable;

import java.util.Collection;
import java.util.List;

@Component
public class ExclusiveController implements ModuleController {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ContainerContext containerContext;

    @Override
    public void onModuleLoad(Module module) {
        for (Module m : moduleService.all()) {
            final Collection<String> excludedPackages = m.releaseExclusive(module);
            if (excludedPackages != null && !excludedPackages.isEmpty()) {
                ThrowingRunnable.sneaky(() -> {
                    final List<ContainerObject> beanDetails = this.containerContext.scanClasses()
                            .name(m.getName() + " - excluded load")
                            .prefix(m.getPlugin().getName() + "-")
                            .classLoader(m.getClassLoader(), this.getClass().getClassLoader())
                            .classPath(excludedPackages)
                            .url(m.getShadedPath().toUri().toURL())
                            .scan();
                    beanDetails.forEach(details -> details.bindWith(m.getPlugin()));
                }).run();
            }
        }
    }

    @Override
    public void onModuleUnload(Module module) {
        // TODO
    }
}
