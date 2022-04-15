package io.fairyproject.tests;

import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public interface TestingHandle {

    Plugin plugin();

    FairyPlatform platform();

    @Nullable String scanPath();

    default boolean shouldInitialize() {
        return true;
    }

}
