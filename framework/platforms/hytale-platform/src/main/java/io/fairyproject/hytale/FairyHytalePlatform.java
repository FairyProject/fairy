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

import com.hypixel.hytale.server.core.plugin.PluginBase;
import io.fairyproject.FairyPlatform;
import io.fairyproject.PlatformType;
import io.fairyproject.hytale.plugin.HytalePluginHandler;
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
    private static Runnable shutdownCallback;

    private final URLClassLoaderAccess classLoader;
    private final File dataFolder;
    private final CompositeTerminable compositeTerminable;

    public FairyHytalePlatform(PluginBase plugin, Runnable shutdownCallback, File dataFolder) {
        FairyPlatform.INSTANCE = this;
        PLUGIN = plugin;
        FairyHytalePlatform.shutdownCallback = shutdownCallback;

        this.dataFolder = dataFolder;
        this.compositeTerminable = CompositeTerminable.create();
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = URLClassLoaderAccess.create((URLClassLoader) classLoader);
        } else {
            this.classLoader = URLClassLoaderAccess.create(null);
        }

        PluginManager.initialize(new HytalePluginHandler());
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
        if (shutdownCallback != null) {
            shutdownCallback.run();
        }
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
