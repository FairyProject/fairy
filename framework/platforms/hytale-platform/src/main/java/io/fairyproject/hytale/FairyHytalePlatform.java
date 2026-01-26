/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.hytale;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import io.fairyproject.PlatformType;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.hytale.entity.RegisterAsChunkSystem;
import io.fairyproject.hytale.entity.RegisterAsEntitySystem;
import io.fairyproject.hytale.logger.HytaleILogger;
import io.fairyproject.hytale.plugin.HytalePluginHandler;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.URLClassLoaderAccess;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URLClassLoader;

public class FairyHytalePlatform extends FairyPlatform implements TerminableConsumer {

    public static PluginBase PLUGIN;

    private final URLClassLoaderAccess classLoader;
    private final File dataFolder;
    private final CompositeTerminable compositeTerminable;
    private final Runnable shutdownCallback;

    public FairyHytalePlatform(PluginBase plugin, Runnable shutdownCallback, File dataFolder) {
        FairyPlatform.INSTANCE = this;
        this.shutdownCallback = shutdownCallback;
        setPlugin(plugin);

        this.dataFolder = dataFolder;
        this.compositeTerminable = CompositeTerminable.create();
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = URLClassLoaderAccess.create((URLClassLoader) classLoader);
        } else {
            this.classLoader = URLClassLoaderAccess.create(null);
        }

        PluginManager.initialize(new HytalePluginHandler());
        if (!Debug.UNIT_TEST) {
            Log.set(new HytaleILogger());
        }
    }

    @SuppressWarnings("unchecked")
    @PreInitialize
    public void onPreInitialize() {
        this.getContainerContext().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(ISystem.class))
                .withAddHandler(ContainerObjCollector.warpInstance(ISystem.class, system -> {
                    if (system.getClass().isAnnotationPresent(RegisterAsEntitySystem.class)) {
                        EntityStore.REGISTRY.registerSystem((ISystem<EntityStore>) system);
                    } else if (system.getClass().isAnnotationPresent(RegisterAsChunkSystem.class)) {
                        ChunkStore.REGISTRY.registerSystem((ISystem<ChunkStore>) system);
                    }
                }))
                .withRemoveHandler(ContainerObjCollector.warpInstance(ISystem.class, system -> {
                    if (system.getClass().isAnnotationPresent(RegisterAsEntitySystem.class)) {
                        EntityStore.REGISTRY.unregisterSystem(
                                (Class<? extends ISystem<EntityStore>>) system.getClass()
                        );
                    } else if (system.getClass().isAnnotationPresent(RegisterAsChunkSystem.class)) {
                        ChunkStore.REGISTRY.unregisterSystem(
                                (Class<? extends ISystem<ChunkStore>>) system.getClass()
                        );
                    }
                })));
    }

    @NotNull
    @Override
    public <T extends Terminable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Override
    public void saveResource(String name, boolean replace) {
        // Hytale doesn't have a built-in saveResource mechanism like Bukkit
        // This can be implemented later if needed
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
        if (this.shutdownCallback != null) {
            this.shutdownCallback.run();
        }
    }

    private static synchronized void setPlugin(PluginBase plugin) {
        PLUGIN = plugin;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.HYTALE;
    }
}
