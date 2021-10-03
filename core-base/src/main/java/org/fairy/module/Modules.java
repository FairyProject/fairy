package org.fairy.module;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class Modules {

    private final Map<String, Module> TYPE_BY_NAME = new ConcurrentHashMap<>();

    @Nullable
    public Module getByName(String name) {
        return TYPE_BY_NAME.get(name);
    }

    public void registerModule(String name, Module module) {
        if (TYPE_BY_NAME.containsKey(name)) {
            throw new UnsupportedOperationException("Name already exists.");
        }

        module.start();
        TYPE_BY_NAME.put(name, module);
    }

    public boolean unregisterModule(String name) {
        final Module module = TYPE_BY_NAME.remove(name);
        if (module != null) {
            module.stop();
            return true;
        }
        return false;
    }

}
