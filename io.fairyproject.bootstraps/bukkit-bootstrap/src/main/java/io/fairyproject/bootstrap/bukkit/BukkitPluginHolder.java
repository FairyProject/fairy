package io.fairyproject.bootstrap.bukkit;

import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.BasePluginHolder;
import io.fairyproject.plugin.PluginAction;

final class BukkitPluginHolder extends BasePluginHolder {

    public BukkitPluginHolder(JsonObject jsonObject) {
        super(jsonObject);
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
