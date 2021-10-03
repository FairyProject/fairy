package org.fairy.bootstrap.bukkit;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.fairy.plugin.Plugin;
import org.fairy.plugin.PluginDescription;
import org.fairy.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;

final class BukkitPluginHolder {

    private final Plugin plugin;
    private final ClassLoader classLoader;

    public BukkitPluginHolder(JsonObject jsonObject, ClassLoader classLoader) {
        PluginDescription pluginDescription = new PluginDescription(jsonObject);

        this.classLoader = classLoader;
        this.plugin = this.findMainClass(pluginDescription.getMainClass());
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
            throw new IllegalStateException("Failed to new instance " + mainClassPath + " (Do you have no args constructor in the class?)");
        }
    }

    public void onLoad() {
        PluginManager.INSTANCE.addPlugin(plugin);
        PluginManager.INSTANCE.onPluginInitial(plugin);

        plugin.onInitial();
    }

    public void onEnable() {
        plugin.onPreEnable();
        PluginManager.INSTANCE.onPluginEnable(plugin);
        plugin.onPluginEnable();
    }

    public void onDisable() {
        try {
            plugin.onPluginDisable();
        } catch (Throwable throwable) {
            LogManager.getLogger().error(throwable);
        }
        try {
            plugin.getCompositeTerminable().close();
        } catch (Throwable throwable) {
            LogManager.getLogger().error(throwable);
        }

        PluginManager.INSTANCE.onPluginDisable(plugin);
    }

}
