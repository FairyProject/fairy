package io.fairytest;

import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.FairyTestingPlatform;
import io.fairyproject.tests.TestingHandle;
import io.fairytest.plugin.PluginMock;
import org.jetbrains.annotations.Nullable;

public class CoreTestingHandle implements TestingHandle {

    public static final PluginMock PLUGIN = new PluginMock();

    @Override
    public Plugin plugin() {
        return PLUGIN;
    }

    @Override
    public FairyPlatform platform(Plugin plugin) {
        return new FairyTestingPlatform(plugin);
    }

    @Override
    public @Nullable String scanPath() {
        return null;
    }

    @Override
    public boolean shouldInitialize() {
        return false;
    }
}
