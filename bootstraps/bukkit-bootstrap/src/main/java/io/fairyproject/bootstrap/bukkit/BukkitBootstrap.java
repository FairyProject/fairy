package io.fairyproject.bootstrap.bukkit;

import org.bukkit.Bukkit;
import io.fairyproject.bootstrap.BaseBootstrap;
import io.fairyproject.bootstrap.BasePlatformBridge;
import io.fairyproject.bootstrap.bukkit.util.BootstrapUtil;
import io.fairyproject.bootstrap.type.PlatformType;
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
