package io.fairyproject.bootstrap.bukkit;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.BasePlatformBridge;
import io.fairyproject.bukkit.FairyBukkitPlatform;

class BukkitPlatformBridge extends BasePlatformBridge {

    private final BukkitBootstrap bootstrap;

    public BukkitPlatformBridge(BukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public FairyPlatform createPlatform() {
        return new FairyBukkitPlatform(this.bootstrap.getBootstrapDirectory().toFile());
    }
}
