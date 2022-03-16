package io.fairyproject.bootstrap.bukkit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;

public final class BukkitPlugin extends JavaPlugin {

    private static final String FAIRY_JSON_PATH = "fairy.json";
    public static JavaPlugin INSTANCE;

    private BukkitBootstrap bootstrap;
    private BukkitPluginHolder pluginHolder;

    private boolean successfulBoot;

    @Override
    public void onLoad() {
        INSTANCE = this;

        JsonObject jsonObject;
        try {
            jsonObject = new Gson().fromJson(new InputStreamReader(this.getResource(FAIRY_JSON_PATH)), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load " + FAIRY_JSON_PATH, throwable);
        }

        this.pluginHolder = new BukkitPluginHolder(jsonObject);

        this.bootstrap = new BukkitBootstrap(this.pluginHolder.getPlugin());
        if (!this.bootstrap.load()) {
            this.getLogger().warning("Failed to boot fairy! check stacktrace for the reason of failure!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.pluginHolder.onLoad();

        this.successfulBoot = true;
    }

    @Override
    public void onEnable() {
        if (!this.successfulBoot) {
            return;
        }
        this.bootstrap.enable();
        this.pluginHolder.onEnable();
    }

    @Override
    public void onDisable() {
        if (!this.successfulBoot) {
            return;
        }
        this.bootstrap.disable();
        this.pluginHolder.onDisable();
    }

    // public extension
    public ClassLoader getPluginClassLoader() {
        return this.getClassLoader();
    }
}
