package org.fairy.bootstrap.bukkit;

import org.fairy.FairyPlatform;
import org.fairy.bootstrap.BasePlatformBridge;
import org.fairy.bukkit.FairyBukkitPlatform;

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
