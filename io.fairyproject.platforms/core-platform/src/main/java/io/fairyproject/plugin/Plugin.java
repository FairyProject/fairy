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

import com.google.common.base.Preconditions;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Getter
public abstract class Plugin implements TerminableConsumer, Terminable {

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    private ClassLoader classLoader;

    private PluginDescription description;
    private PluginAction action;
    private boolean forceDisabling; // ignore every error caused by force disabling

    public void onInitial() {

    }

    public void onPreEnable() {

    }

    public void onPluginEnable() {

    }

    public void onPluginDisable() {

    }

    public void onFrameworkFullyDisable() {

    }

    public final void initializePlugin(PluginDescription description, PluginAction action, ClassLoader classLoader) {
        this.description = description;
        this.action = action;
        this.classLoader = classLoader;
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    public final ClassLoader getPluginClassLoader() {
        return this.classLoader;
    }

    public final String getName() {
        return this.description.getName();
    }

    public Path getDataFolder() {
        return this.action.getDataFolder();
    }

    @Override
    public void close() throws Exception {
        Preconditions.checkNotNull(this.action, "The plugin hasn't been initialized.");

        this.action.close();
    }

    @Override
    public boolean isClosed() {
        return this.action.isClosed();
    }
}
