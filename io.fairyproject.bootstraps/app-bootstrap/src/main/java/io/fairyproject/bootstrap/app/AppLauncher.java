package io.fairyproject.bootstrap.app;

import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.PluginFileReader;
import io.fairyproject.bootstrap.instance.PluginInstance;
import io.fairyproject.bootstrap.internal.FairyInternalIdentityMeta;
import io.fairyproject.bootstrap.platform.PlatformBootstrap;
import lombok.RequiredArgsConstructor;

@FairyInternalIdentityMeta
@RequiredArgsConstructor
public class AppLauncher {

    private final PluginFileReader pluginFileReader;
    private final PluginInstance pluginInstance;
    private final PlatformBootstrap bootstrap;
    private final AppShutdownHookRegistry shutdownHookRegistry;

    protected boolean loaded;

    public boolean start() {
        if (!bootstrap.preload()) {
            System.err.println("Failed to boot fairy! check stacktrace for the reason of failure!");
            return false;
        }

        JsonObject jsonObject = this.pluginFileReader.read(AppLauncher.class);
        pluginInstance.init(jsonObject);
        bootstrap.load(pluginInstance.getPlugin());

        pluginInstance.onLoad();

        bootstrap.enable();
        pluginInstance.onEnable();
        shutdownHookRegistry.register();

        this.loaded = true;
        return true;
    }

}
