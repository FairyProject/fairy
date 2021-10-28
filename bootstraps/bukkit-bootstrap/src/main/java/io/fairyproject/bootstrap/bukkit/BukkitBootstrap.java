package io.fairyproject.bootstrap.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import io.fairyproject.bootstrap.BaseBootstrap;
import io.fairyproject.bootstrap.BasePlatformBridge;
import io.fairyproject.bootstrap.bukkit.util.BootstrapUtil;
import io.fairyproject.bootstrap.type.PlatformType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

@Getter
class BukkitBootstrap extends BaseBootstrap {

    private Plugin plugin;

    @Override
    protected void onFailure(@Nullable Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }
        Bukkit.shutdown();
    }

    @Override
    protected PlatformType getPlatformType() {
        return PlatformType.BUKKIT;
    }

    @Override
    protected BasePlatformBridge createPlatformBridge() {
        return new io.fairyproject.bootstrap.bukkit.BukkitPlatformBridge(this);
    }

    @Override
    public boolean loadJar(Path jarPath) throws Exception {
        this.plugin = Bukkit.getPluginManager().loadPlugin(jarPath.toFile());
        this.swapPluginOrder();

        CLASS_LOADER = this.plugin.getClass().getClassLoader();
        return true;
    }

    private void swapPluginOrder() throws Exception {
        // Hack into Bukkit PluginManager to ensure it loads before this plugin

        final PluginManager pluginManager = Bukkit.getPluginManager();
        final Class<? extends PluginManager> pluginManagerClass = pluginManager.getClass();

        final Field pluginsField = pluginManagerClass.getDeclaredField("plugins");
        pluginsField.setAccessible(true);

        final List<Plugin> plugins = (List<Plugin>) pluginsField.get(pluginManager);
        final int fairyIndex = plugins.indexOf(this.plugin);
        final int pluginIndex = plugins.indexOf(BukkitPlugin.INSTANCE);
        if (fairyIndex == -1 || pluginIndex == -1) {
            // shouldn't really happen
            throw new IllegalStateException("Plugin or Fairy wasn't in plugins list from Bukkit's PluginManager.");
        }

        // swap order
        plugins.set(pluginIndex, this.plugin);
        plugins.set(fairyIndex, BukkitPlugin.INSTANCE);
    }

    @Override
    protected Path getBootstrapDirectory() {
        Path path;
        try {
            path = BootstrapUtil.getPluginDirectory().toPath();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            path = new File(Bukkit.getWorldContainer(), "plugins").toPath();
        }
        return path;
    }
}
