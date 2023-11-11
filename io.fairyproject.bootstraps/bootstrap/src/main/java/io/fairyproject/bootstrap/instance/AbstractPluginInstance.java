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

package io.fairyproject.bootstrap.instance;

import com.google.gson.JsonObject;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.plugin.initializer.PluginClassInitializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class AbstractPluginInstance implements PluginInstance {

    private final PluginClassInitializer pluginClassInitializer;
    protected final CompletableFuture<Plugin> pluginCompletableFuture = new CompletableFuture<>();
    @Getter
    protected Plugin plugin;
    protected ClassLoader classLoader;

    protected abstract ClassLoader getClassLoader();

    protected abstract PluginAction getPluginAction();

    @Override
    public void init(JsonObject jsonObject) {
        PluginDescription pluginDescription = new PluginDescription(jsonObject);
        this.classLoader = pluginClassInitializer.initializeClassLoader(pluginDescription.getName(), this.getClassLoader());
        this.plugin = pluginClassInitializer.create(pluginDescription.getMainClass(), this.classLoader);

        PluginManager.INSTANCE.onPluginPreLoaded(this.classLoader, pluginDescription, this.getPluginAction(), this.pluginCompletableFuture);

        this.plugin.initializePlugin(pluginDescription, this.getPluginAction(), this.classLoader);
    }

    @Override
    public void onLoad() {
        PluginManager.INSTANCE.addPlugin(plugin);
        PluginManager.INSTANCE.onPluginInitial(plugin);

        this.pluginClassInitializer.onPluginLoad(plugin);

        plugin.onInitial();
    }

    @Override
    public void onEnable() {
        plugin.onPreEnable();
        if (plugin.isClosed()) {
            return;
        }
        PluginManager.INSTANCE.onPluginEnable(plugin);
        try {
            plugin.onPluginEnable();
        } catch (Throwable throwable) {
            if (!plugin.isClosed()) {
                Log.error(throwable);
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            plugin.onPluginDisable();
        } catch (Throwable throwable) {
            Log.error(throwable);
        }

        plugin.getCompositeTerminable().closeAndReportException();
        PluginManager.INSTANCE.onPluginDisable(plugin);
        PluginManager.INSTANCE.removePlugin(plugin);

        this.pluginClassInitializer.onPluginUnload(plugin);
    }

}
