package io.fairyproject.bootstrap;

import com.google.gson.JsonObject;
import io.fairyproject.Fairy;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
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
        Fairy.getPluginManager().onPluginPreLoaded(this.classLoader, pluginDescription, this.getPluginAction(), this.pluginCompletableFuture);

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
        Fairy.getPluginManager().addPlugin(plugin);
        Fairy.getPluginManager().onPluginInitial(plugin);

        plugin.onInitial();
    }

    public void onEnable() {
        plugin.onPreEnable();
        if (plugin.isClosed()) {
            return;
        }
        Fairy.getPluginManager().onPluginEnable(plugin);
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
        Fairy.getPluginManager().onPluginDisable(plugin);
    }

}
