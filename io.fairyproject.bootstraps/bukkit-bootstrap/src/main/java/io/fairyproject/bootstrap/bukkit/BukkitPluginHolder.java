package io.fairyproject.bootstrap.bukkit;

import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.BasePluginHolder;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.initializer.PluginClassInitializer;

final class BukkitPluginHolder extends BasePluginHolder {

    public BukkitPluginHolder(PluginClassInitializer initializer, JsonObject jsonObject) {
        super(initializer, jsonObject);
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
