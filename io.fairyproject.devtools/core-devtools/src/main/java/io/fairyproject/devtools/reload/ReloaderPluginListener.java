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

package io.fairyproject.devtools.reload;

import io.fairyproject.container.PostInitialize;
import io.fairyproject.devtools.watcher.ClasspathFileAlterationListener;
import io.fairyproject.devtools.watcher.ClasspathFileWatcher;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.plugin.PluginManager;
import lombok.RequiredArgsConstructor;

import java.net.URISyntaxException;
import java.net.URL;

@RequiredArgsConstructor
public class ReloaderPluginListener implements PluginListenerAdapter {

    private final ClasspathFileWatcher classpathFileWatcher;
    private final ClasspathCollection classpathCollection;

    @PostInitialize
    public void onPostInitialize() {
        PluginManager.INSTANCE.registerListener(this);
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        URL url = classpathCollection.getURLByName(plugin.getName());
        if (url == null)
            return;

        try {
            classpathFileWatcher.addURL(url, new ClasspathFileAlterationListener(plugin));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot add classpath monitor to watcher", e);
        }
    }

    @Override
    public void onPluginDisable(Plugin plugin) {
        URL url = classpathCollection.getURLByName(plugin.getName());
        if (url == null)
            return;

        classpathFileWatcher.removeURL(url);
    }
}
