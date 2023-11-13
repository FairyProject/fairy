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

package io.fairyproject.devtools.bukkit;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.devtools.bukkit.plugin.BukkitPluginManagerWrapper;
import io.fairyproject.devtools.bukkit.plugin.PaperPluginManagerWrapper;
import io.fairyproject.devtools.bukkit.plugin.PluginManagerWrapper;
import io.fairyproject.devtools.reload.Reloader;
import io.fairyproject.log.Log;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;

@InjectableComponent
public class BukkitPluginReloaderSetup {

    public BukkitPluginReloaderSetup(Reloader reloader, BukkitDependencyResolver dependencyResolver, MCSchedulerProvider mcSchedulerProvider) {
        PluginManagerWrapper pluginManagerWrapper;
        try {
            Class.forName("io.papermc.paper.plugin.manager.PaperPluginManagerImpl");
            pluginManagerWrapper = new PaperPluginManagerWrapper();

            Log.info("Paper detected, using PaperPluginManagerImpl");
        } catch (ClassNotFoundException e) {
            pluginManagerWrapper = new BukkitPluginManagerWrapper();

            Log.info("Paper not detected, using BukkitPluginManager");
        }

        reloader.setScheduler(mcSchedulerProvider.getGlobalScheduler());

        BukkitPluginCache pluginFileCache = new BukkitPluginCache();
        DefaultPluginLoadingStrategy pluginLoadingStrategy = new DefaultPluginLoadingStrategy();

        reloader.setReloadStartupHandler(new BukkitReloadStartupHandler(pluginManagerWrapper, pluginFileCache, pluginLoadingStrategy));
        reloader.setReloadShutdownHandler(new BukkitReloadShutdownHandler(pluginManagerWrapper, dependencyResolver, pluginFileCache));
    }

}
