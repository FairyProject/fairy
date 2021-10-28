package io.fairyproject.bootstrap.bukkit;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.BasePlatformBridge;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import org.bukkit.Bukkit;

class BukkitPlatformBridge extends BasePlatformBridge {

    private final BukkitBootstrap bootstrap;

    public BukkitPlatformBridge(BukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public FairyPlatform createPlatform() {
        return null;
    }

    @Override
    public void load() {
        String message = String.format("Loading %s (Boot from plugin)", bootstrap.getPlugin().getDescription().getFullName());
        System.out.println(message);

        bootstrap.getPlugin().onLoad();
    }

    @Override
    public void enable() {
        if (Bukkit.getPluginManager().isPluginEnabled("Fairy")) {
            return;
        }
        Bukkit.getPluginManager().enablePlugin(FairyBukkitPlatform.PLUGIN);
    }

    @Override
    public void disable() {

    }
}
