package io.fairyproject.bootstrap.bukkit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;

public final class BukkitPlugin extends JavaPlugin {

    private static final String FAIRY_JSON_PATH = "fairy.json";
    public static JavaPlugin INSTANCE;

    private BukkitBootstrap bootstrap;
    private BukkitPluginHolder pluginHolder;

    private boolean loaded;

    @Override
    public void onLoad() {
        INSTANCE = this;

        JsonObject jsonObject;
        try {
            final InputStream resource = this.getResource(FAIRY_JSON_PATH);
            if (resource == null) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to find fairy.json in jar resource.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            jsonObject = new Gson().fromJson(new InputStreamReader(resource), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load " + FAIRY_JSON_PATH, throwable);
        }

        this.bootstrap = new BukkitBootstrap();
        if (!this.bootstrap.preload()) {
            this.getLogger().warning("Failed to boot fairy! check stacktrace for the reason of failure!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.pluginHolder = new BukkitPluginHolder(jsonObject);
        this.bootstrap.load(this.pluginHolder.getPlugin());
        this.pluginHolder.onLoad();

        this.loaded = true;
    }

    @Override
    public void onEnable() {
        if (!this.loaded) {
            return;
        }
        this.bootstrap.enable();
        this.pluginHolder.onEnable();
    }

    @Override
    public void onDisable() {
        if (!this.loaded) {
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
