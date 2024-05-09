package io.fairyproject.tests;

import io.fairyproject.FairyPlatform;
import io.fairyproject.PlatformType;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.URLClassLoaderAccess;

import java.io.File;
import java.net.URLClassLoader;

public class FairyTestingPlatform extends FairyPlatform {

    public FairyTestingPlatform() {
        if (!PluginManager.isInitialized()) {
            PluginManager.initialize(type -> {
                if (type.getName().startsWith("io.fairytest")) {
                    return "test";
                }
                return null;
            });
        }
    }

    @Override
    public void load(Plugin plugin) {
        super.load(plugin);
    }

    @Override
    public void enable() {
        super.enable();
    }

    @Override
    public URLClassLoaderAccess getClassloader() {
        return URLClassLoaderAccess.create((URLClassLoader) this.getClass().getClassLoader());
    }

    @Override
    public File getDataFolder() {
        return new File("build/tmp/fairy");
    }

    @Override
    public void saveResource(String name, boolean replace) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public PlatformType getPlatformType() {
        throw new UnsupportedOperationException("Not Implemented.");
    }
}
