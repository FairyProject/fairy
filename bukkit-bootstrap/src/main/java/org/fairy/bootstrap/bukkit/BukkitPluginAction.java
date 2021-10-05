package org.fairy.bootstrap.bukkit;

import org.bukkit.Bukkit;
import org.fairy.plugin.PluginAction;

public class BukkitPluginAction implements PluginAction {

    private final BukkitPlugin bukkitPlugin;

    public BukkitPluginAction(BukkitPlugin bukkitPlugin) {
        this.bukkitPlugin = bukkitPlugin;
    }

    @Override
    public void close() {
        Bukkit.getPluginManager().disablePlugin(this.bukkitPlugin);
    }

    @Override
    public boolean isClosed() {
        return this.bukkitPlugin.isEnabled();
    }
}
