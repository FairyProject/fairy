package io.fairyproject.bootstrap;

import com.google.gson.JsonObject;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.plugin.initializer.PluginClassInitializer;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

public abstract class BasePluginHolder {

    private final PluginClassInitializer pluginClassInitializer;
    @Getter
    protected final Plugin plugin;
    protected final ClassLoader classLoader;
    protected final CompletableFuture<Plugin> pluginCompletableFuture = new CompletableFuture<>();

    public BasePluginHolder(PluginClassInitializer initializer, JsonObject jsonObject) {
        this.pluginClassInitializer = initializer;

        PluginDescription pluginDescription = new PluginDescription(jsonObject);
        ClassLoader classLoader = initializer.initializeClassLoader(pluginDescription.getName(), this.getClassLoader());

        PluginManager.INSTANCE.onPluginPreLoaded(classLoader, pluginDescription, this.getPluginAction(), this.pluginCompletableFuture);

        this.plugin = initializer.create(pluginDescription.getMainClass(), classLoader);
        this.plugin.initializePlugin(pluginDescription, this.getPluginAction(), classLoader);
        this.classLoader = classLoader;

        this.pluginCompletableFuture.complete(this.plugin);
    }

    protected abstract ClassLoader getClassLoader();

    protected abstract PluginAction getPluginAction();

    public void onLoad() {
        PluginManager.INSTANCE.addPlugin(plugin);
        PluginManager.INSTANCE.onPluginInitial(plugin);

        this.pluginClassInitializer.onPluginLoad(plugin);

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
            if (!plugin.isClosed()) {
                Log.error(throwable);
            }
        }
    }

    public void onDisable() {
        try {
            plugin.onPluginDisable();
        } catch (Throwable throwable) {
            Log.error(throwable);
        }

        plugin.getCompositeTerminable().closeAndReportException();
        PluginManager.INSTANCE.onPluginDisable(plugin);
    }

}
