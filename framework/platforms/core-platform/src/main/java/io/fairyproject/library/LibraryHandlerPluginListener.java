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

package io.fairyproject.library;

import io.fairyproject.Debug;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.util.URLClassLoaderAccess;
import lombok.RequiredArgsConstructor;

import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class LibraryHandlerPluginListener implements PluginListenerAdapter {

    private final LibraryHandlerImpl libraryHandler;

    @Override
    public void onPluginPreLoaded(ClassLoader classLoader, PluginDescription description, PluginAction action, CompletableFuture<Plugin> completableFuture) {
        if (Debug.UNIT_TEST)
            return;

        for (Library library : description.getLibraries()) {
            this.libraryHandler.loadLibrary(library, true);
        }
    }

    @Override
    public void onPluginInitial(Plugin plugin) {
        final URLClassLoaderAccess classLoader = URLClassLoaderAccess.create((URLClassLoader) plugin.getPluginClassLoader());
        this.libraryHandler.addClassLoader(plugin, classLoader);
    }

    @Override
    public void onPluginDisable(Plugin plugin) {
        this.libraryHandler.getPluginClassLoaders().remove(plugin);
    }
}
