package io.fairytest;

import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.FairyTestingPlatform;
import io.fairyproject.tests.TestingHandle;
import io.fairytest.plugin.MockPlugin;
import org.jetbrains.annotations.Nullable;

public class CoreTestingHandle implements TestingHandle {

    public static final MockPlugin PLUGIN = new MockPlugin();

    @Override
    public Plugin plugin() {
        return PLUGIN;
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
