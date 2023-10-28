package io.fairyproject.bootstrap.bukkit;

import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.platform.PlatformBootstrap;
import io.fairyproject.bootstrap.PluginClassInitializerFinder;
import io.fairyproject.bootstrap.PluginFileReader;
import io.fairyproject.bootstrap.instance.PluginInstance;
import io.fairyproject.bootstrap.internal.FairyInternalIdentityMeta;
import lombok.AccessLevel;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

@FairyInternalIdentityMeta
public final class BukkitPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    private final PluginManager pluginManager;
    private final PluginInstance instance;
    private final PluginFileReader pluginFileReader;
    private final PlatformBootstrap bootstrap;

    @Setter(AccessLevel.PACKAGE)
    private boolean loaded;


    public BukkitPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file,
                        PluginManager pluginManager,
                        PluginInstance instance,
                        PluginFileReader pluginFileReader,
                        PlatformBootstrap bootstrap) {
        super(loader, description, dataFolder, file);
        this.pluginManager = pluginManager;
        this.instance = instance;
        this.pluginFileReader = pluginFileReader;
        this.bootstrap = bootstrap;
    }

    public BukkitPlugin(PluginManager pluginManager,
                        PluginInstance instance,
                        PluginFileReader pluginFileReader,
                        PlatformBootstrap bootstrap) {
        this.pluginManager = pluginManager;
        this.instance = instance;
        this.pluginFileReader = pluginFileReader;
        this.bootstrap = bootstrap;
    }

    public BukkitPlugin() {
        this(
                Bukkit.getPluginManager(),
                new BukkitPluginInstance(PluginClassInitializerFinder.find()),
                new PluginFileReader(),
                new BukkitPlatformBootstrap()
        );
    }

    @Override
    public void onLoad() {
        INSTANCE = this;

        if (!this.bootstrap.preload()) {
            this.getLogger().warning("Failed to boot fairy! check stacktrace for the reason of failure!");
            pluginManager.disablePlugin(this);
            return;
        }

        JsonObject jsonObject = pluginFileReader.read(this.getClass());
        this.instance.init(jsonObject);
        this.bootstrap.load(this.instance.getPlugin());
        this.instance.onLoad();

        this.loaded = true;
    }

    @Override
    public void onEnable() {
        if (!this.loaded)
            throw new IllegalStateException("Plugin not loaded yet!");

        this.bootstrap.enable();
        this.instance.onEnable();
    }

    @Override
    public void onDisable() {
        if (!this.loaded)
            return;

        this.instance.onDisable();
        this.bootstrap.disable();
    }

    // public extension
    public ClassLoader getPluginClassLoader() {
        return this.getClassLoader();
    }
}
