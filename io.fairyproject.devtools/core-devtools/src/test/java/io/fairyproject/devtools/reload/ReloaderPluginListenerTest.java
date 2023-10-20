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

import io.fairyproject.devtools.watcher.ClasspathFileAlterationListener;
import io.fairyproject.devtools.watcher.ClasspathFileWatcher;
import io.fairyproject.mock.MockPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

class ReloaderPluginListenerTest {

    private ClasspathFileWatcher classpathFileWatcher;
    private ClasspathCollection classpathCollection;
    private ReloaderPluginListener reloaderPluginListener;

    @BeforeEach
    void setUp() {
        classpathFileWatcher = Mockito.mock(ClasspathFileWatcher.class);
        classpathCollection = new ClasspathCollection();
        reloaderPluginListener = new ReloaderPluginListener(
                classpathFileWatcher,
                classpathCollection
                );
    }

    @Test
    void onPluginEnableShouldAddMonitor() throws MalformedURLException, URISyntaxException {
        MockPlugin plugin = new MockPlugin();
        URL url = Paths.get("test").toUri().toURL();
        classpathCollection.addURL("test", url);

        reloaderPluginListener.onPluginEnable(plugin);

        Mockito.verify(classpathFileWatcher).addURL(eq(url), Mockito.any(ClasspathFileAlterationListener.class));
    }

    @Test
    void onPluginEnableWithoutURLShouldDoNothing() throws URISyntaxException {
        MockPlugin plugin = new MockPlugin();

        reloaderPluginListener.onPluginEnable(plugin);

        Mockito.verify(classpathFileWatcher, Mockito.never()).addURL(Mockito.any(), Mockito.any());
    }

    @Test
    void onPluginDisableShouldRemoveMonitor() throws MalformedURLException {
        MockPlugin plugin = new MockPlugin();
        URL url = Paths.get("test").toUri().toURL();
        classpathCollection.addURL("test", url);

        reloaderPluginListener.onPluginDisable(plugin);

        Mockito.verify(classpathFileWatcher).removeURL(eq(url));
    }

    @Test
    void onPluginDisableWithoutURLShouldDoNothing() {
        MockPlugin plugin = new MockPlugin();

        reloaderPluginListener.onPluginDisable(plugin);

        Mockito.verify(classpathFileWatcher, Mockito.never()).removeURL(Mockito.any());
    }

}