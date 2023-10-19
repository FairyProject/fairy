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
import io.fairyproject.devtools.reload.ReloadStartupHandler;
import io.fairyproject.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;

import java.io.File;
import java.net.URL;

@RequiredArgsConstructor
public class BukkitReloadStartupHandler implements ReloadStartupHandler {

    private final Server server;

    @Override
    public void start(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        org.bukkit.plugin.java.JavaPlugin javaPlugin = JavaPluginUtil.getProvidingPlugin(plugin.getClass());
        if (javaPlugin == null) {
            throw new IllegalStateException("JavaPlugin is null");
        }

        // reload the plugin entirely
        URL url = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        org.bukkit.plugin.Plugin newBukkitPlugin;
        try {
            File file = new File(url.toURI());
            newBukkitPlugin = server.getPluginManager().loadPlugin(file);
            if (newBukkitPlugin == null) {
                throw new IllegalStateException("Failed to load plugin");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load plugin", ex);
        }
        // bukkit plugin manager doesn't call onLoad() when only enabling plugin
        newBukkitPlugin.onLoad();
        server.getPluginManager().enablePlugin(newBukkitPlugin);
    }
}
