package org.fairy.bootstrap.bukkit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;

public final class BukkitPlugin extends JavaPlugin {

    protected static final String FAIRY_JSON_PATH = "fairy.json";
    public static BukkitPlugin INSTANCE;

    private BukkitBootstrap bootstrap;
    private BukkitPluginHolder pluginHolder;

    @Override
    public void onLoad() {
        INSTANCE = this;

        this.bootstrap = new BukkitBootstrap();
        this.bootstrap.load();

        JsonObject jsonObject;
        try {
            jsonObject = new Gson().fromJson(new InputStreamReader(this.getResource(FAIRY_JSON_PATH)), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load " + FAIRY_JSON_PATH, throwable);
        }

        this.pluginHolder = new BukkitPluginHolder(jsonObject, this.getClassLoader());
        this.pluginHolder.onLoad();
    }

    @Override
    public void onEnable() {
        this.bootstrap.enable();
        this.pluginHolder.onEnable();
    }

    @Override
    public void onDisable() {
        this.bootstrap.disable();
        this.pluginHolder.onDisable();
    }
}
