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

package io.fairyproject.plugin;

import io.fairyproject.mock.MockPlugin;
import io.fairyproject.tests.TestingContext;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class PluginManagerTest {

    private PluginHandler pluginHandler;
    private PluginManager pluginManager;

    @BeforeAll
    static void beforeAll() {
        TestingContext.get().shutdown();
    }

    @BeforeEach
    void setUp() {
        pluginHandler = Mockito.mock(PluginHandler.class);
        PluginManager.initialize(pluginHandler);

        this.pluginManager = PluginManager.INSTANCE;
    }

    @AfterEach
    void tearDown() {
        this.pluginManager.unload();
    }

    @Test
    void initializeShouldSetInstance() {
        assertNotNull(PluginManager.INSTANCE);
        assertTrue(PluginManager.isInitialized());
    }

    @Test
    void addPluginShouldAddPlugin() {
        Plugin plugin = new MockPlugin("test");

        this.pluginManager.addPlugin(plugin);

        assertEquals(plugin, this.pluginManager.getPlugin("test"));
    }

    @Test
    void addPluginShouldAvoidDuplicate() {
        Plugin plugin = new MockPlugin("test");

        this.pluginManager.addPlugin(plugin);

        assertThrows(IllegalArgumentException.class, () -> this.pluginManager.addPlugin(plugin));
    }

    @Test
    void getPluginShouldReturnNull() {
        assertNull(this.pluginManager.getPlugin("test"));
    }

    @Test
    void getPluginShouldIgnoreCase() {
        Plugin plugin = new MockPlugin("test");

        this.pluginManager.addPlugin(plugin);

        assertEquals(plugin, this.pluginManager.getPlugin("TEST"));
    }

    @Test
    void removePluginShouldRemovePlugin() {
        Plugin plugin = new MockPlugin("test");

        this.pluginManager.addPlugin(plugin);

        assertEquals(plugin, this.pluginManager.getPlugin("test"));

        this.pluginManager.removePlugin(plugin);

        assertNull(this.pluginManager.getPlugin("test"));
    }

    @Test
    void removePluginShouldThrowException() {
        Plugin plugin = new MockPlugin("test");

        assertThrows(IllegalArgumentException.class, () -> this.pluginManager.removePlugin(plugin));
    }

    @Test
    void getPluginsShouldReturnEmpty() {
        assertTrue(this.pluginManager.getPlugins().isEmpty());
    }

    @Test
    void getPluginsShouldReturnPlugins() {
        Plugin plugin = new MockPlugin("test");
        MockPlugin plugin2 = new MockPlugin("test2");

        this.pluginManager.addPlugin(plugin);
        this.pluginManager.addPlugin(plugin2);

        assertEquals(2, this.pluginManager.getPlugins().size());
        assertTrue(this.pluginManager.getPlugins().contains(plugin));
        assertTrue(this.pluginManager.getPlugins().contains(plugin2));
    }

    @Test
    void getPluginByClassShouldReturnNull() {
        assertNull(this.pluginManager.getPluginByClass(MockPlugin.class));
    }

    @Test
    void testUnload() {
        Plugin plugin = new MockPlugin("test");
        this.pluginManager.addPlugin(plugin);

        this.pluginManager.unload();

        assertNull(this.pluginManager.getPlugin("test"));
        assertTrue(this.pluginManager.getPlugins().isEmpty());
        assertTrue(this.pluginManager.getListeners().isEmpty());
        assertFalse(PluginManager.isInitialized());
    }

    @Test
    void getPluginByClassShouldReturnPlugin() {
        Plugin plugin = new MockPlugin("test");
        Mockito.when(pluginHandler.getPluginByClass(MockPlugin.class)).thenReturn("test");

        this.pluginManager.addPlugin(plugin);

        assertEquals(plugin, this.pluginManager.getPluginByClass(MockPlugin.class));
    }

    @Nested
    class Listeners {

        private List<PluginListenerAdapter> listeners;

        @BeforeEach
        void setUp() {
            listeners = new ArrayList<>();

            int numListeners = 5;
            for (int i = 0; i < numListeners; i++) {
                PluginListenerAdapter listener = Mockito.mock(PluginListenerAdapter.class);
                Mockito.when(listener.priority()).thenReturn(i);

                listeners.add(listener);
                pluginManager.registerListener(listener);
            }
        }

        @Test
        void registerListenerShouldAddListenerInOrder() {
            List<PluginListenerAdapter> registeredListeners = pluginManager.getListeners();

            assertEquals(listeners.size(), registeredListeners.size());
            assertTrue(registeredListeners.containsAll(listeners));
            int j = registeredListeners.size() - 1;
            for (int i = 0; i < registeredListeners.size(); i++) {
                assertEquals(listeners.get(i), registeredListeners.get(j--));
            }
        }

        @Test
        void onPluginPreLoadedShouldCallListener() {
            ClassLoader classLoader = Mockito.mock(ClassLoader.class);
            PluginDescription description = Mockito.mock(PluginDescription.class);
            PluginAction action = Mockito.mock(PluginAction.class);
            CompletableFuture<Plugin> completableFuture = new CompletableFuture<>();

            pluginManager.onPluginPreLoaded(classLoader, description, action, completableFuture);

            listeners.forEach(listenerAdapter -> {
                Mockito.verify(listenerAdapter).onPluginPreLoaded(classLoader, description, action, completableFuture);
            });
        }

        @Test
        void onPluginInitialShouldCallListener() {
            Plugin plugin = new MockPlugin();

            pluginManager.onPluginInitial(plugin);

            listeners.forEach(listenerAdapter -> {
                Mockito.verify(listenerAdapter).onPluginInitial(plugin);
            });
        }

        @Test
        void onPluginEnableShouldCallListener() {
            Plugin plugin = new MockPlugin();

            pluginManager.onPluginEnable(plugin);

            listeners.forEach(listenerAdapter -> {
                Mockito.verify(listenerAdapter).onPluginEnable(plugin);
            });
        }

        @Test
        void onPluginDisableShouldCallListener() {
            Plugin plugin = new MockPlugin();

            pluginManager.onPluginDisable(plugin);

            listeners.forEach(listenerAdapter -> {
                Mockito.verify(listenerAdapter).onPluginDisable(plugin);
            });
        }

        @Test
        void removeListenerShouldRemoveListener() {
            PluginListenerAdapter listener = listeners.get(0);

            pluginManager.removeListener(listener);

            assertFalse(pluginManager.getListeners().contains(listener));
            assertSame(listeners.size() - 1, pluginManager.getListeners().size());
        }

    }
}