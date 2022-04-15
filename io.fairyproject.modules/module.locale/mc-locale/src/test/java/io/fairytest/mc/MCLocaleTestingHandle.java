package io.fairytest.mc;

import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.FairyTestingPlatform;
import io.fairyproject.tests.TestingHandle;
import org.jetbrains.annotations.Nullable;

public class MCLocaleTestingHandle implements TestingHandle {
    @Override
    public Plugin plugin() {
        return new Plugin() {};
    }

    @Override
    public FairyPlatform platform() {
        return new FairyTestingPlatform();
    }

    @Override
    public @Nullable String scanPath() {
        return "io.fairytest.mc";
    }
}
