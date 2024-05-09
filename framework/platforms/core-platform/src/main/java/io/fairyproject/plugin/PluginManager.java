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

package io.fairyproject.plugin;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PluginManager {

    public static PluginManager INSTANCE;

    private final Map<String, Plugin> plugins;
    @Getter
    private final List<PluginListenerAdapter> listeners;
    private final PluginHandler pluginHandler;

    public PluginManager(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;

        this.plugins = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
    }

    public void unload() {
        this.plugins.clear();
        this.listeners.clear();
        INSTANCE = null;
    }

    public void onPluginPreLoaded(ClassLoader classLoader,
                                  PluginDescription description,
                                  PluginAction action,
                                  CompletableFuture<Plugin> completableFuture) {
        synchronized (this.listeners) {
            this.listeners.forEach(listenerAdapter -> listenerAdapter.onPluginPreLoaded(classLoader, description, action, completableFuture));
        }
    }

    public void onPluginInitial(Plugin plugin) {
        synchronized (this.listeners) {
            this.listeners.forEach(listenerAdapter -> listenerAdapter.onPluginInitial(plugin));
        }
    }

    public void onPluginEnable(Plugin plugin) {
        synchronized (this.listeners) {
            this.listeners.forEach(listenerAdapter -> {
                listenerAdapter.onPluginEnable(plugin);
            });
        }
    }

    public void onPluginDisable(Plugin plugin) {
        synchronized (this.listeners) {
            this.listeners.forEach(listenerAdapter -> listenerAdapter.onPluginDisable(plugin));
        }
    }

    public Collection<Plugin> getPlugins() {
        return this.plugins.values();
    }

    public Plugin getPlugin(String name) {
        return this.plugins.get(name.toLowerCase());
    }

    public void addPlugin(Plugin plugin) {
        String name = plugin.getName().toLowerCase();
        if (this.plugins.containsKey(name))
            throw new IllegalArgumentException("Plugin " + name + " already registered!");

        this.plugins.put(name, plugin);
    }

    public void removePlugin(Plugin plugin) {
        String name = plugin.getName().toLowerCase();
        if (!this.plugins.containsKey(name))
            throw new IllegalArgumentException("Plugin " + plugin.getName() + " not registered!");

        this.plugins.remove(name);
    }

    public void callFrameworkFullyDisable() {
        this.plugins.values().forEach(Plugin::onFrameworkFullyDisable);
    }

    public void registerListener(PluginListenerAdapter listenerAdapter) {
        synchronized (this.listeners) {
            this.listeners.add(listenerAdapter);
            this.listeners.sort(Collections.reverseOrder(Comparator.comparingInt(PluginListenerAdapter::priority)));
        }
    }

    public void removeListener(PluginListenerAdapter listenerAdapter) {
        synchronized (this.listeners) {
            this.listeners.remove(listenerAdapter);
        }
    }

    @Nullable
    public Plugin getPluginByClass(Class<?> type) {
        String name = this.pluginHandler.getPluginByClass(type);
        if (name == null) {
            return null;
        }

        return this.getPlugin(name);
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public static void initialize(PluginHandler pluginHandler) {
        if (INSTANCE != null) {
            throw new IllegalArgumentException("Don't Initialize twice!");
        }

        INSTANCE = new PluginManager(pluginHandler);
    }

}
