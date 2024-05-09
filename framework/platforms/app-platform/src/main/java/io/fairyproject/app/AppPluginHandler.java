package io.fairyproject.app;

import io.fairyproject.Fairy;
import io.fairyproject.plugin.PluginHandler;
import org.jetbrains.annotations.Nullable;

public class AppPluginHandler implements PluginHandler {
    @Override
    public @Nullable String getPluginByClass(Class<?> type) {
        final FairyAppPlatform platform = (FairyAppPlatform) Fairy.getPlatform();
        return platform.isAppClass(type) ? platform.getMainApplication().getName() : null;
    }
}
