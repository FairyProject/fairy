package io.fairyproject.bootstrap.bukkit;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.BaseBootstrap;
import io.fairyproject.bootstrap.bukkit.util.BootstrapUtil;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.plugin.Plugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

@Getter
class BukkitBootstrap extends BaseBootstrap {

    public BukkitBootstrap(Plugin plugin) {
        super(plugin);
    }

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
    protected FairyPlatform createPlatform() {
        return new FairyBukkitPlatform(this.plugin, BukkitPlugin.INSTANCE.getDataFolder());
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
