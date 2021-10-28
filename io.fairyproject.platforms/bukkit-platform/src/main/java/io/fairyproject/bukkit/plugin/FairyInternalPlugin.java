package io.fairyproject.bukkit.plugin;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public class FairyInternalPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        FairyBukkitPlatform.PLUGIN = this;
        FairyBukkitPlatform.INSTANCE = new FairyBukkitPlatform(this.getDataFolder());
        FairyBukkitPlatform.INSTANCE.load();
    }

    @Override
    public void onEnable() {
        FairyBukkitPlatform.INSTANCE.enable();
    }

    @Override
    public void onDisable() {
        FairyBukkitPlatform.INSTANCE.disable();
    }

}

