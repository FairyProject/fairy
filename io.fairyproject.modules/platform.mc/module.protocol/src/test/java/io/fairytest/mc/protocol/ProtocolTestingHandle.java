package io.fairytest.mc.protocol;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.bukkit.BukkitServerMockImpl;
import io.fairyproject.tests.bukkit.BukkitTestingHandle;
import io.fairyproject.tests.bukkit.FairyBukkitTestingPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProtocolTestingHandle implements BukkitTestingHandle {
    @Override
    public Plugin plugin() {
        return new Plugin() {};
    }

    @Override
    public FairyPlatform platform() {
        return new FairyBukkitTestingPlatform() {
            @Override
            public OBCVersion version() {
                return OBCVersion.v1_16_R3;
            }
        };
    }

    @Override
    public @Nullable String scanPath() {
        return "io.fairytest";
    }

    @Override
    public @NotNull be.seeseemelk.mockbukkit.ServerMock createServerMock() {
        return new BukkitServerMockImpl();
    }
}
