package io.fairyproject.bootstrap.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import io.fairyproject.bootstrap.BaseBootstrap;
import io.fairyproject.bootstrap.BasePlatformBridge;
import io.fairyproject.bootstrap.bukkit.util.BootstrapUtil;
import io.fairyproject.bootstrap.type.PlatformType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

@Getter
class BukkitBootstrap extends BaseBootstrap {

    private Plugin plugin;

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
        return new io.fairyproject.bootstrap.bukkit.BukkitPlatformBridge(this);
    }

    @Override
    public boolean loadJar(Path jarPath) throws Exception {
        this.plugin = Bukkit.getPluginManager().loadPlugin(jarPath.toFile());

        CLASS_LOADER = this.plugin.getClass().getClassLoader();
        return true;
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
