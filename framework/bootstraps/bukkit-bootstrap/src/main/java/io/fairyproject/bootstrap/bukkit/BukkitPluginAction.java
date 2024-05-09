package io.fairyproject.bootstrap.bukkit;

import org.bukkit.Bukkit;
import io.fairyproject.plugin.PluginAction;

import java.nio.file.Path;

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
        return !this.bukkitPlugin.isEnabled();
    }

    @Override
    public Path getDataFolder() {
        return this.bukkitPlugin.getDataFolder().toPath();
    }
}
