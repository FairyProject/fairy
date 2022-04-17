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

package io.fairyproject.bukkit.impl;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.plugin.PluginHandler;
import io.fairyproject.util.AccessUtil;
import io.github.toolfactory.narcissus.Narcissus;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class BukkitPluginHandler implements PluginHandler {

    private final Field field;

    public BukkitPluginHandler() {
        try {
            final Class<?> pluginClassLoader = Narcissus.findClass("org.bukkit.plugin.java.PluginClassLoader");
            this.field = Narcissus.findField(pluginClassLoader, "plugin");
            AccessUtil.setAccessible(field);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public @Nullable String getPluginByClass(Class<?> type) {
        try {
            JavaPlugin plugin = JavaPluginUtil.getProvidingPlugin(type);
            if (plugin != null) {
                return plugin.getName();
            }
        } catch (Throwable ignored) {}

        try {
            ClassLoader classLoader = type.getClassLoader();

            final Plugin plugin = (Plugin) this.field.get(classLoader);
            return plugin.getName();
        } catch (Throwable ignored) {
            return FairyBukkitPlatform.PLUGIN.getName();
        }
    }

}
