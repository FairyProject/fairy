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
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

class BukkitReloadShutdownHandlerTest {

    private PluginManagerWrapper pluginManager;
    private BukkitDependencyResolver dependencyResolver;
    private BukkitReloadShutdownHandler bukkitReloadShutdownHandler;
    private BukkitPluginCache pluginCache;
    private MockPlugin fairyPlugin;
    private JavaPlugin javaPlugin;

    @BeforeAll
    static void setUpAll() {
        MockBukkitContext.get().initialize();
    }

    @BeforeEach
    void setUp() {
        pluginManager = Mockito.mock(PluginManagerWrapper.class);
        dependencyResolver = Mockito.mock(BukkitDependencyResolver.class);
        pluginCache = new BukkitPluginCache();
        bukkitReloadShutdownHandler = new BukkitReloadShutdownHandler(pluginManager, dependencyResolver, pluginCache);

        fairyPlugin = new MockPlugin();
        javaPlugin = MockBukkit.createMockPlugin("Main");

        RootJavaPluginIdentifier.getInstance().addFirst(new SpecifyJavaPluginIdentifier(javaPlugin));
    }

    @Test
    void shutdownPluginMustNotBeNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> bukkitReloadShutdownHandler.shutdown(null));
    }

    @Test
    void testShutdown() {
        bukkitReloadShutdownHandler.shutdown(fairyPlugin);

        Mockito.verify(pluginManager).disablePlugin(javaPlugin);
    }

    @Test
    void shutdownShouldDisableDependedPlugins() {
        be.seeseemelk.mockbukkit.MockPlugin dependA = MockBukkit.createMockPlugin("MockPlugin1");
        be.seeseemelk.mockbukkit.MockPlugin dependB = MockBukkit.createMockPlugin("MockPlugin2");
        Mockito.when(dependencyResolver.resolveDependsBy(javaPlugin)).thenReturn(Collections.singletonList(dependA));
        Mockito.when(dependencyResolver.resolveDependsBy(dependA)).thenReturn(Collections.singletonList(dependB));

        bukkitReloadShutdownHandler.shutdown(fairyPlugin);

        Mockito.verify(pluginManager).disablePlugin(dependA);
        Mockito.verify(pluginManager).disablePlugin(dependB);
    }

    @Test
    void shutdownShouldCachePluginFileAndDependent() {
        be.seeseemelk.mockbukkit.MockPlugin dependA = MockBukkit.createMockPlugin("MockPlugin1");
        be.seeseemelk.mockbukkit.MockPlugin dependB = MockBukkit.createMockPlugin("MockPlugin2");
        Mockito.when(dependencyResolver.resolveDependsBy(javaPlugin)).thenReturn(Collections.singletonList(dependA));
        Mockito.when(dependencyResolver.resolveDependsBy(dependA)).thenReturn(Collections.singletonList(dependB));

        bukkitReloadShutdownHandler.shutdown(fairyPlugin);

        String path = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Assertions.assertEquals(path, pluginCache.getSource(javaPlugin.getName()).toString());

        path = dependA.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Assertions.assertEquals(path, pluginCache.getSource(dependA.getName()).toString());

        path = dependB.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Assertions.assertEquals(path, pluginCache.getSource(dependB.getName()).toString());
        Assertions.assertEquals(Collections.singletonList(dependA.getName()), pluginCache.getDependents(javaPlugin.getName()));
        Assertions.assertEquals(Collections.singletonList(dependB.getName()), pluginCache.getDependents(dependA.getName()));
    }
}