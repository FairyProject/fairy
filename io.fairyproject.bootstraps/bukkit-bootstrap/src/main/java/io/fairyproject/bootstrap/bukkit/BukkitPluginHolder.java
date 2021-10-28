package io.fairyproject.bootstrap.bukkit;

import com.google.gson.JsonObject;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationTargetException;

final class BukkitPluginHolder {

    private final Plugin plugin;
    private final ClassLoader classLoader;

    public BukkitPluginHolder(JsonObject jsonObject, BukkitPlugin bukkitPlugin) {
        PluginDescription pluginDescription = new PluginDescription(jsonObject);

        this.classLoader = bukkitPlugin.getPluginClassLoader();
        this.plugin = this.findMainClass(pluginDescription.getMainClass());
        this.plugin.initializePlugin(pluginDescription, new BukkitPluginAction(bukkitPlugin), this.classLoader);
    }

    private Plugin findMainClass(String mainClassPath) {
        Class<?> mainClass;
        try {
            mainClass = Class.forName(mainClassPath, true, this.classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load mainClass " + mainClassPath, e);
        }

        if (!Plugin.class.isAssignableFrom(mainClass)) {
            throw new IllegalStateException("Couldn't found no args constructor from " + mainClassPath);
        }

        try {
            return (Plugin) mainClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to new instance " + mainClassPath + " (Do it has no args constructor in the class?)");
        }
    }

    public void onLoad() {
        PluginManager.INSTANCE.addPlugin(plugin);
        PluginManager.INSTANCE.onPluginInitial(plugin);

        plugin.onInitial();
    }

    public void onEnable() {
        plugin.onPreEnable();
        if (plugin.isClosed()) {
            return;
        }
        PluginManager.INSTANCE.onPluginEnable(plugin);
        try {
            plugin.onPluginEnable();
        } catch (Throwable throwable) {
            if (!plugin.isClosed() && !plugin.isForceDisabling()) {
                LogManager.getLogger(plugin.getClass()).error(throwable);
            }
        }
    }

    public void onDisable() {
        try {
            plugin.onPluginDisable();
        } catch (Throwable throwable) {
            if (!plugin.isForceDisabling()) {
                LogManager.getLogger(plugin.getClass()).error(throwable);
            }
        }

        plugin.getCompositeTerminable().closeAndReportException();
        PluginManager.INSTANCE.onPluginDisable(plugin);
    }

}
