package io.example.debug;

import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.FairyPlatform;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.bukkit.BukkitServerMockImpl;
import io.fairyproject.tests.bukkit.BukkitTestingHandle;
import io.fairyproject.tests.bukkit.FairyBukkitTestingPlatform;

public class DebugTestingHandle implements BukkitTestingHandle {
    @Override
    public ServerMock createServerMock() {
        return new BukkitServerMockImpl();
    }

    @Override
    public Plugin plugin() {
        return new DebugPlugin();
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
    public String scanPath() {
        return "io.example.debug";
    }
}
