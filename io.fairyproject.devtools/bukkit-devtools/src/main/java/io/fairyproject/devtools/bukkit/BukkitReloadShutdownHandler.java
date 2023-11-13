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

import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.devtools.bukkit.plugin.PluginManagerWrapper;
import io.fairyproject.devtools.reload.ReloadShutdownHandler;
import io.fairyproject.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BukkitReloadShutdownHandler implements ReloadShutdownHandler {

    private final PluginManagerWrapper pluginManager;
    private final BukkitDependencyResolver dependencyResolver;
    private final BukkitPluginCache pluginCache;

    @Override
    public void shutdown(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        JavaPlugin javaPlugin = JavaPluginUtil.getProvidingPlugin(plugin.getClass());
        if (javaPlugin == null) {
            throw new IllegalStateException("JavaPlugin is null");
        }

        this.shutdownBukkitPlugin(javaPlugin);
    }

    private void shutdownBukkitPlugin(org.bukkit.plugin.Plugin plugin) {
        List<String> dependents = new ArrayList<>();
        for (org.bukkit.plugin.Plugin bukkitPlugin : this.dependencyResolver.resolveDependsBy(plugin)) {
            dependents.add(bukkitPlugin.getName());

            pluginCache.addSource(bukkitPlugin.getName(), this.getPluginSource(bukkitPlugin));
            this.shutdownBukkitPlugin(bukkitPlugin);
        }

        pluginCache.addSource(plugin.getName(), this.getPluginSource(plugin));
        pluginCache.addDependents(plugin.getName(), dependents);
        pluginManager.disablePlugin(plugin);
    }

    private Path getPluginSource(org.bukkit.plugin.Plugin plugin) {
        URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to get plugin source", e);
        }
    }
}
