package io.fairyproject.debug;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bukkit.reflection.minecraft.MinecraftVersion;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.TestingHandle;
import io.fairyproject.tests.bukkit.FairyBukkitTestingPlatform;

public class DebugTestingHandle implements TestingHandle {
    @Override
    public Plugin plugin() {
        return new DebugPlugin();
    }

    @Override
    public FairyPlatform platform() {
        ContainerContext.SHOW_LOGS = true;
        return new FairyBukkitTestingPlatform() {
            @Override
            public MinecraftVersion version() {
                return new MinecraftVersion("v1_16_R3", 11603);
            }
        };
    }

    @Override
    public String scanPath() {
        return "io.fairyproject.debug";
    }
}
