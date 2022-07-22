package io.fairyproject.bootstrap;

import com.google.gson.JsonObject;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

public abstract class BasePluginHolder {

    @Getter
    protected final Plugin plugin;
    protected final ClassLoader classLoader;
    protected final CompletableFuture<Plugin> pluginCompletableFuture = new CompletableFuture<>();

    public BasePluginHolder(JsonObject jsonObject) {
        PluginDescription pluginDescription = new PluginDescription(jsonObject);

        this.classLoader = this.getClassLoader();
        PluginManager.INSTANCE.onPluginPreLoaded(this.classLoader, pluginDescription, this.getPluginAction(), this.pluginCompletableFuture);

        this.plugin = this.findMainClass(pluginDescription.getMainClass());
        this.plugin.initializePlugin(pluginDescription, this.getPluginAction(), this.classLoader);
        this.pluginCompletableFuture.complete(this.plugin);
    }

    protected abstract ClassLoader getClassLoader();

    protected abstract PluginAction getPluginAction();

    private Plugin findMainClass(String mainClassPath) {
        Class<?> mainClass;
        try {
            mainClass = Class.forName(mainClassPath, true, this.classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load mainClass " + mainClassPath, e);
        }

        if (!Plugin.class.isAssignableFrom(mainClass)) {
            throw new IllegalStateException(String.format("%s wasn't implementing Plugin", mainClass));
        }

        try {
            return (Plugin) mainClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to new instance " + mainClassPath + " (Does it has no args constructor in the class?)", e);
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
                Log.error(throwable);
            }
        }
    }

    public void onDisable() {
        try {
            plugin.onPluginDisable();
        } catch (Throwable throwable) {
            if (!plugin.isForceDisabling()) {
                Log.error(throwable);
            }
        }

        plugin.getCompositeTerminable().closeAndReportException();
        PluginManager.INSTANCE.onPluginDisable(plugin);
    }

}
