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

package org.fairy.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.fairy.FairyBootstrap;
import org.fairy.bukkit.events.PostServicesInitialEvent;
import org.fairy.bukkit.impl.ComponentHolderBukkitListener;
import org.fairy.bukkit.util.SpigotUtil;
import org.fairy.bean.ComponentRegistry;
import org.fairy.FairyPlatform;
import org.fairy.bukkit.impl.BukkitPluginHandler;
import org.fairy.bukkit.impl.BukkitTaskScheduler;
import org.fairy.library.Library;
import org.fairy.plugin.PluginClassLoader;
import org.fairy.plugin.PluginManager;
import org.fairy.task.ITaskScheduler;

import java.util.HashSet;
import java.util.Set;

public final class FairyBukkitPlatform extends JavaPlugin implements FairyPlatform {

    private PluginClassLoader pluginClassLoader;
    private FairyBootstrap bootstrap;

    @Override
    public void onLoad() {
        Imanity.PLUGIN = this;
        PluginManager.initialize(new BukkitPluginHandler());

        this.pluginClassLoader = new PluginClassLoader(this.getClassLoader());
    }

    @Override
    public void onEnable() {
        SpigotUtil.init();
        ComponentRegistry.registerComponentHolder(new ComponentHolderBukkitListener());

        this.bootstrap = new FairyBootstrap(this);
        this.bootstrap.enable();
    }

    @Override
    public void onDisable() {
        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.stop();
        }
        this.bootstrap.disable();
    }

    @Override
    public void onPostServicesInitial() {
        Imanity.callEvent(new PostServicesInitialEvent());
    }

    @Override
    public PluginClassLoader getClassloader() {
        return this.pluginClassLoader;
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public boolean isRunning() {
        return this.isEnabled();
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public ITaskScheduler createTaskScheduler() {
        return new BukkitTaskScheduler();
    }

    @Override
    public Set<Library> getDependencies() {
        Set<Library> libraries = new HashSet<>();
        if (SpigotUtil.SPIGOT_TYPE != SpigotUtil.SpigotType.IMANITY) {
            libraries.add(Library.FAST_UTIL);
        }
        return libraries;
    }
}
