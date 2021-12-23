package io.fairyproject.debug;

import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.TestingHandle;
import io.fairyproject.tests.bukkit.FairyBukkitTestingPlatform;
import org.jetbrains.annotations.Nullable;

public class DebugTestingHandle implements TestingHandle {
    @Override
    public Plugin plugin() {
        return new DebugPlugin();
    }

    @Override
    public FairyPlatform platform() {
        return new FairyBukkitTestingPlatform();
    }

    @Override
    public @Nullable String scanPath() {
        return "io.fairyproject.debug";
    }
}
