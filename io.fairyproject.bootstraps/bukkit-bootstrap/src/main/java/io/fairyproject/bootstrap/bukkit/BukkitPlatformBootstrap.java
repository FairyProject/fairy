package io.fairyproject.bootstrap.bukkit;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.platform.AbstractPlatformBootstrap;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@Getter
class BukkitPlatformBootstrap extends AbstractPlatformBootstrap {

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
        return new FairyBukkitPlatform(BukkitPlugin.INSTANCE.getDataFolder());
    }
}
