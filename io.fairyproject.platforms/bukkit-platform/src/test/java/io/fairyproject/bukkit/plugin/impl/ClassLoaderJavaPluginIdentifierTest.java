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

package io.fairyproject.bukkit.plugin.impl;

import env.fake.FakeServer;
import io.fairyproject.mock.MockPlugin;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginClassLoader;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.*;

class ClassLoaderJavaPluginIdentifierTest {

    private ClassLoaderJavaPluginIdentifier classLoaderJavaPluginIdentifier;
    private SamplePluginClassLoader pluginClassLoader;
    private Server server;
    private JavaPlugin javaPlugin;

    @BeforeEach
    void setUp() {
        server = Mockito.mock(Server.class);
        PluginManager pluginManager = Mockito.mock(PluginManager.class);
        Mockito.when(server.getPluginManager()).thenReturn(pluginManager);

        javaPlugin = Mockito.mock(JavaPlugin.class);
        Mockito.when(pluginManager.getPlugin("test")).thenReturn(javaPlugin);

        classLoaderJavaPluginIdentifier = new ClassLoaderJavaPluginIdentifier(server);

        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
        pluginClassLoader = new SamplePluginClassLoader(new MockPlugin(), classLoader);
    }

    @Test
    void findByClassShouldReturnNullByDefault() {
        Class<?> mockClass = FakeServer.class;

        assertNull(classLoaderJavaPluginIdentifier.findByClass(mockClass));
    }

    @Test
    void findByClassWithCorrectLoaderShouldReturnPlugin() throws ClassNotFoundException {
        Class<?> mockClass = pluginClassLoader.loadClass("env.fake.FakeServer");

        assertEquals(javaPlugin, classLoaderJavaPluginIdentifier.findByClass(mockClass));
    }

    private static class SamplePluginClassLoader extends URLClassLoader implements PluginClassLoader {

        private final Plugin plugin;

        public SamplePluginClassLoader(Plugin plugin, URLClassLoader urlClassLoader) {
            super(urlClassLoader.getURLs());
            this.plugin = plugin;
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return this.plugin;
        }

        @Override
        public void addURL(@NotNull URL url) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass == null) {
                    try {
                        loadedClass = findClass(name);
                    } catch (ClassNotFoundException ex) {
                        loadedClass = Class.forName(name, false, getParent());
                    }
                }

                if (resolve) {
                    resolveClass(loadedClass);
                }

                return loadedClass;
            }
        }
    }

}