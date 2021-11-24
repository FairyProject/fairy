package io.fairyproject.module.controller;

import io.fairyproject.bean.Autowired;
import io.fairyproject.bean.BeanContext;
import io.fairyproject.bean.Component;
import io.fairyproject.bean.details.BeanDetails;
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
    private BeanContext beanContext;

    @Override
    public void onModuleLoad(Module module) {
        for (Module m : moduleService.all()) {
            final Collection<String> excludedPackages = m.releaseExclusive(module);
            if (excludedPackages != null && !excludedPackages.isEmpty()) {
                ThrowingRunnable.unchecked(() -> {
                    final List<BeanDetails> beanDetails = this.beanContext.scanClasses()
                            .name(m.getName() + " - excluded load")
                            .prefix(m.getPlugin().getName() + "-")
                            .mainClassloader(m.getClassLoader())
                            .classLoader(this.getClass().getClassLoader())
                            .classPath(excludedPackages)
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
