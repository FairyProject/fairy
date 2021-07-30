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

package org.fairy.bukkit.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.fairy.plugin.AbstractPlugin;
import org.fairy.plugin.PluginManager;
import org.fairy.util.Utility;
import org.fairy.util.terminable.TerminableConsumer;
import org.fairy.util.terminable.composite.CompositeTerminable;
import org.jetbrains.annotations.NotNull;

public abstract class BukkitPlugin extends JavaPlugin implements AbstractPlugin, TerminableConsumer {

    protected final Logger logger = LogManager.getLogger();
    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Override
    public final void onLoad() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        PluginManager.INSTANCE.addPlugin(this);
        PluginManager.INSTANCE.onPluginInitial(this);

        this.onInitial();
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public final void onEnable() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());

        Utility.resolveLinkageError();

        this.onPreEnable();

        PluginManager.INSTANCE.onPluginEnable(this);

        this.onPluginEnable();
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public final void onDisable() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        try {
            this.onPluginDisable();
        } catch (Throwable throwable) {
            this.logger.error(throwable);
        }
        try {
            this.compositeTerminable.close();
        } catch (Throwable throwable) {
            this.logger.error(throwable);
        }

        PluginManager.INSTANCE.onPluginDisable(this);
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public final void close() {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public final ClassLoader getPluginClassLoader() {
        return this.getClassLoader();
    }

}
