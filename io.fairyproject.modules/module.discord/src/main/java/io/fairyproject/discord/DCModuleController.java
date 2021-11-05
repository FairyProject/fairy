package io.fairyproject.discord;

import io.fairyproject.bean.Component;
import io.fairyproject.module.Module;
import io.fairyproject.module.ModuleController;

@Component
public class DCModuleController implements ModuleController {
    @Override
    public void onModuleLoad(Module module) {
        if (module.getName().equals("core-command")) {

        }
    }

    @Override
    public void onModuleUnload(Module module) {

    }
}
