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

package io.fairyproject.devtools.bukkit.plugin;

import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import io.fairyproject.util.AccessUtil;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class PaperPluginManagerWrapper implements PluginManagerWrapper {

    private final Object instance;
    private final ObjectWrapper objectWrapper;

    public PaperPluginManagerWrapper() {
        try {
            Class<?> pluginManagerClass = Class.forName("io.papermc.paper.plugin.manager.PaperPluginManagerImpl");
            Object pluginManager = pluginManagerClass.getMethod("getInstance").invoke(null);
            Field field = pluginManager.getClass().getDeclaredField("instanceManager");
            AccessUtil.setAccessible(field);
            this.instance = field.get(pluginManager);
            this.objectWrapper = new ObjectWrapper(instance);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to find PaperPluginManagerImpl class", e);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to get PaperPluginManagerImpl instance", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to get instanceManager field", e);
        }
    }

    @Override
    public Plugin loadPlugin(@NotNull File file) {
        return (Plugin) objectWrapper.getMethod("loadPlugin", Path.class).invoke(instance, file.toPath());
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        objectWrapper.getMethod("enablePlugin", Plugin.class).invoke(instance, plugin);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        objectWrapper.getMethod("disablePlugin", Plugin.class).invoke(instance, plugin);
        Map<String, Plugin> map = objectWrapper.getField("lookupNames");
        List<Plugin> list = objectWrapper.getField("plugins");

        map.remove(plugin.getName().toLowerCase());
        list.remove(plugin);

        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            try {
                ((URLClassLoader) classLoader).close();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to close class loader", e);
            }
        }

        System.gc();
        System.runFinalization();
    }

    @Override
    public Plugin getPlugin(String name) {
        return (Plugin) objectWrapper.getMethod("getPlugin", String.class).invoke(instance, name);
    }
}
