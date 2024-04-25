package io.fairyproject.bootstrap.bukkit;

import io.fairyproject.bootstrap.instance.AbstractPluginInstance;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.initializer.PluginClassInitializer;

final class BukkitPluginInstance extends AbstractPluginInstance {

    public BukkitPluginInstance(PluginClassInitializer initializer) {
        super(initializer);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return ((BukkitPlugin) BukkitPlugin.INSTANCE).getPluginClassLoader();
    }

    @Override
    protected PluginAction getPluginAction() {
        return new BukkitPluginAction((BukkitPlugin) BukkitPlugin.INSTANCE);
    }
}
