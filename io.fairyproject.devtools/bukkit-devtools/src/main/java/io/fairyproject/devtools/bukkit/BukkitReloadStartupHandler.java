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

import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import io.fairyproject.devtools.bukkit.plugin.PluginManagerWrapper;
import io.fairyproject.devtools.reload.ReloadStartupHandler;
import io.fairyproject.plugin.Plugin;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

@RequiredArgsConstructor
public class BukkitReloadStartupHandler implements ReloadStartupHandler {

    private final PluginManagerWrapper pluginManagerWrapper;
    private final BukkitPluginCache pluginCache;
    private final PluginLoadingStrategy pluginLoadingStrategy;

    @Override
    public void start(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        org.bukkit.plugin.java.JavaPlugin javaPlugin = RootJavaPluginIdentifier.getInstance().findByClass(plugin.getClass());
        if (javaPlugin == null) {
            Path path = pluginCache.getSource(plugin.getName());
            if (path == null)
                throw new IllegalStateException("Plugin file not found");

            this.load(path);
            return;
        }

        this.load(javaPlugin);
    }

    private void load(Path path) {
        org.bukkit.plugin.Plugin newBukkitPlugin = pluginManagerWrapper.loadPlugin(path.toFile());
        if (newBukkitPlugin == null) {
            throw new IllegalStateException("Failed to load plugin");
        }

        enable(newBukkitPlugin);
    }

    private void load(org.bukkit.plugin.Plugin javaPlugin) {
        URL url = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        org.bukkit.plugin.Plugin newBukkitPlugin;

        if (pluginLoadingStrategy.shouldLoadFromFile(javaPlugin)) {
            // reload the plugin entirely
            try {
                File file = new File(url.toURI());
                newBukkitPlugin = pluginManagerWrapper.loadPlugin(file);
                if (newBukkitPlugin == null) {
                    throw new IllegalStateException("Failed to load plugin");
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to load plugin", ex);
            }
        } else {
            newBukkitPlugin = javaPlugin;
        }

        enable(newBukkitPlugin);
    }

    private void enable(org.bukkit.plugin.Plugin newBukkitPlugin) {
        // bukkit plugin manager doesn't call onLoad() when only enabling plugin
        newBukkitPlugin.onLoad();
        pluginManagerWrapper.enablePlugin(newBukkitPlugin);

        for (String depend : this.pluginCache.getDependents(newBukkitPlugin.getName())) {
            Path path = this.pluginCache.getSource(depend);
            System.out.println("Loading " + depend + " from " + path);
            if (path == null) {
                throw new IllegalStateException("Plugin file not found");
            }

            this.load(path);
        }
    }
}
