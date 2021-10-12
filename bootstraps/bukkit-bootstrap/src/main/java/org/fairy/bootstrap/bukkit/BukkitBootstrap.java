package org.fairy.bootstrap.bukkit;

import org.bukkit.Bukkit;
import org.fairy.bootstrap.BaseBootstrap;
import org.fairy.bootstrap.BasePlatformBridge;
import org.fairy.bootstrap.bukkit.util.BootstrapUtil;
import org.fairy.bootstrap.type.PlatformType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

class BukkitBootstrap extends BaseBootstrap {
    @Override
    protected void onFailure(@Nullable Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }
        Bukkit.shutdown();
    }

    @Override
    protected PlatformType getPlatformType() {
        return PlatformType.BUKKIT;
    }

    @Override
    protected BasePlatformBridge createPlatformBridge() {
        return new BukkitPlatformBridge(this);
    }

    @Override
    protected Path getBootstrapDirectory() {
        Path path;
        try {
            path = BootstrapUtil.getPluginDirectory().toPath();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            path = new File(Bukkit.getWorldContainer(), "plugins").toPath();
        }
        return path;
    }
}
