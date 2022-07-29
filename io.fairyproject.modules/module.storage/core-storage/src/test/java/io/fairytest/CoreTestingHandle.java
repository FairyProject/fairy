package io.fairytest;

import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.FairyTestingPlatform;
import io.fairyproject.tests.TestingHandle;
import org.jetbrains.annotations.Nullable;

public class CoreTestingHandle implements TestingHandle {

    @Override
    public Plugin plugin() {
        return new PluginMock();
    }

    @Override
    public FairyPlatform platform() {
        return new FairyTestingPlatform();
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
