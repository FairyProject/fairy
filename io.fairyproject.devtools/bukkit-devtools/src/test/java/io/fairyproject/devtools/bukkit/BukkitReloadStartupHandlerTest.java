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

package io.fairyproject.devtools.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import io.fairyproject.bukkit.plugin.impl.SpecifyJavaPluginIdentifier;
import io.fairyproject.devtools.bukkit.plugin.PluginManagerWrapper;
import io.fairyproject.mock.MockPlugin;
import io.fairyproject.tests.bukkit.MockBukkitContext;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

class BukkitReloadStartupHandlerTest {

    private PluginManagerWrapper pluginManager;
    private BukkitReloadStartupHandler bukkitReloadStartupHandler;
    private BukkitPluginCache pluginFileCache;
    private PluginLoadingStrategy pluginLoadingStrategy;
    private MockPlugin fairyPlugin;
    private JavaPlugin javaPlugin;
    private be.seeseemelk.mockbukkit.MockPlugin newJavaPlugin;

    @BeforeAll
    static void setUpAll() {
        MockBukkitContext.get().initialize();
    }

    @BeforeEach
    void setUp() {
        pluginManager = Mockito.mock(PluginManagerWrapper.class);
        pluginFileCache = new BukkitPluginCache();
        pluginLoadingStrategy = Mockito.mock(PluginLoadingStrategy.class);
        bukkitReloadStartupHandler = new BukkitReloadStartupHandler(pluginManager, pluginFileCache, pluginLoadingStrategy);

        fairyPlugin = new MockPlugin();
        MockBukkit.getOrCreateMock();
        javaPlugin = Mockito.spy(MockBukkit.createMockPlugin("a"));
        newJavaPlugin = Mockito.spy(MockBukkit.createMockPlugin("b"));

        Mockito.when(pluginManager.loadPlugin(Mockito.any(File.class))).thenReturn(newJavaPlugin);

        RootJavaPluginIdentifier.getInstance().addFirst(new SpecifyJavaPluginIdentifier(javaPlugin));
    }

    @AfterEach
    void tearDown() {
        RootJavaPluginIdentifier.clearInstance();
    }

    @Test
    void startPluginMustNotBeNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> bukkitReloadStartupHandler.start(null));
    }

    @Test
    void testStart() {
        Mockito.when(pluginLoadingStrategy.shouldLoadFromFile(Mockito.any())).thenReturn(false);
        bukkitReloadStartupHandler.start(fairyPlugin);

        verifyPluginEnabled(javaPlugin, null);
    }

    @Test
    void shouldLoadFromFile() throws URISyntaxException {
        Mockito.when(pluginLoadingStrategy.shouldLoadFromFile(Mockito.any())).thenReturn(true);
        bukkitReloadStartupHandler.start(fairyPlugin);
        // make sure the plugin is reloaded
        URL url = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.toURI());

        verifyPluginEnabled(newJavaPlugin, file);
    }

    @Test
    void javaPluginNotFoundShouldLoadFromFileCache() throws URISyntaxException {
        RootJavaPluginIdentifier.getInstance().clear(); // clear the identifier
        Mockito.when(pluginLoadingStrategy.shouldLoadFromFile(Mockito.any())).thenReturn(false);
        URL url = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(url.toURI());
        pluginFileCache.addSource("test", path);
        Mockito.when(pluginManager.loadPlugin(Mockito.any(File.class))).thenReturn(null);
        Mockito.when(pluginManager.loadPlugin(path.toFile())).thenReturn(newJavaPlugin);

        bukkitReloadStartupHandler.start(fairyPlugin);

        verifyPluginEnabled(newJavaPlugin, path.toFile());
    }

    @Test
    void startShouldEnableDependedPlugins() throws URISyntaxException {
        pluginFileCache.addDependents("b", java.util.Collections.singletonList("NewPluginA"));
        pluginFileCache.addDependents("NewPluginA", java.util.Collections.singletonList("NewPluginB"));
        Path aFile = Paths.get("a");
        pluginFileCache.addSource("NewPluginA", aFile);
        Path bFile = Paths.get("b");
        pluginFileCache.addSource("NewPluginB", bFile);
        Mockito.when(pluginLoadingStrategy.shouldLoadFromFile(Mockito.any())).thenReturn(true);

        AtomicInteger counter = new AtomicInteger();
        JavaPlugin newA = Mockito.spy(MockBukkit.createMockPlugin("NewPluginA"));
        JavaPlugin newB = Mockito.spy(MockBukkit.createMockPlugin("NewPluginB"));

        Mockito.when(pluginManager.loadPlugin(Mockito.any(File.class))).thenAnswer(invocation -> {
            switch (counter.getAndIncrement()) {
                case 0:
                    return newJavaPlugin;
                case 1:
                    return newA;
                case 2:
                    return newB;
                default:
                    throw new IllegalStateException("Unexpected value: " + counter.get());
            }
        });

        bukkitReloadStartupHandler.start(fairyPlugin);

        verifyPluginEnabled(newA, aFile.toFile());
        verifyPluginEnabled(newB, bFile.toFile());

        URL url = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.toURI());

        Mockito.verify(pluginManager, Mockito.atLeast(1)).loadPlugin(file);
    }

    private void verifyPluginEnabled(Plugin plugin, @Nullable File file) {
        if (file != null)
            Mockito.verify(pluginManager, Mockito.atLeast(1)).loadPlugin(file);
        Mockito.verify(plugin).onLoad();
        Mockito.verify(pluginManager).enablePlugin(plugin);
    }
}