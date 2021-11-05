package io.fairyproject.module;

public interface ModuleController {

    void onModuleLoad(Module module);

    void onModuleUnload(Module module);

}
