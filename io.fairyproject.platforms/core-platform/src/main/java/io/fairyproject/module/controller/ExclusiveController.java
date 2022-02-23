package io.fairyproject.module.controller;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.Component;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.scanner.ClassPathScanner;
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
                    final ClassPathScanner classPathScanner = this.containerContext.scanClasses()
                            .name(m.getName() + " - excluded load")
                            .prefix(m.getPlugin().getName() + "-")
                            .classLoader(this.getClass().getClassLoader())
                            .classPath(excludedPackages)
                            .url(m.getShadedPath().toUri().toURL());
                    classPathScanner.scan();

                    final List<ContainerObject> beanDetails = classPathScanner.getCompletedFuture().join();
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
