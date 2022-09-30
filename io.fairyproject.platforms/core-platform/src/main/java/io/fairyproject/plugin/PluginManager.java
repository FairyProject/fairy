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

import io.fairyproject.internal.Process;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PluginManager implements Process {

    private final Map<String, Plugin> plugins;
    private final Set<PluginListenerAdapter> listenerAdapters;
    private final PluginHandler pluginHandler;

    public PluginManager(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;

        this.plugins = new ConcurrentHashMap<>();
        this.listenerAdapters = new TreeSet<>(Collections.reverseOrder(Comparator.comparingInt(PluginListenerAdapter::priority)));
    }

    @Override
    public void destroy() {
        this.plugins.clear();
        this.listenerAdapters.clear();
    }

    public Collection<ClassLoader> getClassLoaders() {
        return this.plugins.values()
                .stream()
                .map(Plugin::getPluginClassLoader)
                .collect(Collectors.toList());
    }

    public void onPluginPreLoaded(ClassLoader classLoader,
                                  PluginDescription description,
                                  PluginAction action,
                                  CompletableFuture<Plugin> completableFuture) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginPreLoaded(classLoader, description, action, completableFuture));
        }
    }

    public void onPluginInitial(Plugin plugin) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginInitial(plugin));
        }
    }

    public void onPluginEnable(Plugin plugin) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginEnable(plugin));
        }
    }

    public void onPluginDisable(Plugin plugin) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginDisable(plugin));
        }
    }

    public Collection<Plugin> getPlugins() {
        return this.plugins.values();
    }

    public Plugin getPlugin(String name) {
        return this.plugins.get(name);
    }

    public void addPlugin(Plugin plugin) {
        this.plugins.put(plugin.getName(), plugin);
    }

    public void callFrameworkFullyDisable() {
        this.plugins.values().forEach(Plugin::onFrameworkFullyDisable);
    }

    public void registerListener(PluginListenerAdapter listenerAdapter) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.add(listenerAdapter);
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

}
