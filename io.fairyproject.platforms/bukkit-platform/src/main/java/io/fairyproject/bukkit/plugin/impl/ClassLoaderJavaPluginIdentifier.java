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

package io.fairyproject.bukkit.plugin.impl;

import io.fairyproject.bukkit.plugin.JavaPluginIdentifier;
import io.fairyproject.plugin.PluginClassLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ClassLoaderJavaPluginIdentifier implements JavaPluginIdentifier {

    private final Server server;

    @Override
    public JavaPlugin findByClass(@NotNull Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader instanceof PluginClassLoader) {
            PluginClassLoader pluginClassLoader = (PluginClassLoader) classLoader;
            String name = pluginClassLoader.getPlugin().getName();

            return (JavaPlugin) server.getPluginManager().getPlugin(name);
        }

        return null;
    }
}
