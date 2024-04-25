/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit;

import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import io.fairyproject.PlatformType;
import io.fairyproject.bukkit.events.PostServicesInitialEvent;
import io.fairyproject.bukkit.impl.BukkitPluginHandler;
import io.fairyproject.bukkit.listener.FilteredListener;
import io.fairyproject.bukkit.listener.RegisterAsListener;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.logger.Log4jLogger;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.bukkit.util.SpigotUtil;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.URLClassLoaderAccess;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URLClassLoader;

public class FairyBukkitPlatform extends FairyPlatform implements TerminableConsumer {

    public static JavaPlugin PLUGIN = JavaPluginUtil.getProvidingPlugin(FairyBukkitPlatform.class);

    private final URLClassLoaderAccess classLoader;
    private final File dataFolder;
    private final CompositeTerminable compositeTerminable;

    @NotNull
    @Override
    public <T extends Terminable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    public FairyBukkitPlatform(File dataFolder) {
        FairyPlatform.INSTANCE = this;

        this.dataFolder = dataFolder;
        this.compositeTerminable = CompositeTerminable.create();
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = URLClassLoaderAccess.create((URLClassLoader) classLoader);
        } else {
            this.classLoader = URLClassLoaderAccess.create(null);
        }

        PluginManager.initialize(new BukkitPluginHandler());
        // Use log4j for bukkit platform
        if (!Debug.UNIT_TEST)
            Log.set(new Log4jLogger());
    }

    @Override
    public void load(Plugin plugin) {
        super.load(plugin);
    }

    @Override
    public void enable() {
        SpigotUtil.init();
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();

        RootJavaPluginIdentifier.clearInstance();
        Metadata.destroy();
    }

    @PreInitialize
    public void onPreInitialize() {
        // TODO: move these to DI container when DI is improved
        this.getContainerContext().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(Listener.class))
                .withFilter(ContainerObjCollector.inherits(FilteredListener.class).negate())
                .withAddHandler(ContainerObjCollector.warpInstance(Listener.class, listener -> {
                    if (!listener.getClass().isAnnotationPresent(RegisterAsListener.class))
                        return;

                    Events.subscribe(listener);
                }))
                .withRemoveHandler(ContainerObjCollector.warpInstance(Listener.class, HandlerList::unregisterAll)));
    }

    @Override
    public void onPostServicesInitial() {
        Events.call(new PostServicesInitialEvent());
    }

    @Override
    public void saveResource(String name, boolean replace) {
        PLUGIN.saveResource(name, replace);
    }

    @Override
    public URLClassLoaderAccess getClassloader() {
        return this.classLoader;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.BUKKIT;
    }
}
