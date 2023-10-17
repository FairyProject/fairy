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
import io.fairyproject.devtools.reload.ReloadShutdownHandler;
import io.fairyproject.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BukkitReloadShutdownHandler implements ReloadShutdownHandler {

    private final Server server;

    @Override
    public void shutdown(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        JavaPlugin javaPlugin = JavaPluginUtil.getProvidingPlugin(plugin.getClass());
        if (javaPlugin == null) {
            throw new IllegalStateException("JavaPlugin is null");
        }

        server.getPluginManager().disablePlugin(javaPlugin);
    }
}
