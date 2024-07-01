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

package io.fairyproject.bootstrap.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.PluginFileReader;
import io.fairyproject.bootstrap.instance.PluginInstance;
import io.fairyproject.bootstrap.platform.PlatformBootstrap;
import io.fairyproject.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class BukkitPluginTest {

    private PluginManager pluginManager;
    private PluginInstance pluginInstance;
    private PluginFileReader pluginFileReader;
    private PlatformBootstrap platformBootstrap;
    private BukkitPlugin bukkitPlugin;

    @BeforeEach
    void setUp() throws IOException {
        ServerMock server = MockBukkit.mock();
        this.pluginManager = Mockito.mock(PluginManager.class);
        this.pluginInstance = Mockito.mock(PluginInstance.class);
        this.pluginFileReader = Mockito.mock(PluginFileReader.class);
        this.platformBootstrap = Mockito.mock(PlatformBootstrap.class);
        PluginDescriptionFile file = new PluginDescriptionFile("fairy", "0.0.1", "io.fairyproject.bootstrap.bukkit.BukkitPlugin");
        this.bukkitPlugin = new BukkitPlugin(
                new JavaPluginLoader(server),
                file,
                Files.createTempDirectory("test-plugin").toFile(),
                Files.createTempFile("test-plugin", ".jar").toFile(),
                this.pluginManager,
                this.pluginInstance,
                this.pluginFileReader,
                this.platformBootstrap
        );
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    class OnLoad {

        @Test
        void preloadFailedShouldDisablePlugin() {
            Mockito.when(platformBootstrap.preload()).thenReturn(false);

            bukkitPlugin.onLoad();

            Mockito.verify(pluginManager).disablePlugin(bukkitPlugin);
        }

        @Test
        void testFullLoad() {
            Plugin plugin = Mockito.mock(Plugin.class);

            JsonObject file = new JsonObject();
            Mockito.when(platformBootstrap.preload()).thenReturn(true);
            Mockito.when(pluginFileReader.read(Mockito.any())).thenReturn(file);
            Mockito.when(pluginInstance.getPlugin()).thenReturn(plugin);

            bukkitPlugin.onLoad();

            assertSame(bukkitPlugin, BukkitPlugin.INSTANCE);
            Mockito.verify(pluginInstance).init(file);
            Mockito.verify(platformBootstrap).load(plugin);
            Mockito.verify(pluginInstance).onLoad();
            Mockito.verify(pluginManager, Mockito.never()).disablePlugin(bukkitPlugin);
        }

    }

    @Nested
    class OnEnable {

        @Test
        void whenNotLoadedShouldThrowException() {
            bukkitPlugin.setLoaded(false);

            assertThrows(IllegalStateException.class, bukkitPlugin::onEnable);
        }

        @Test
        void testFullEnable() {
            bukkitPlugin.setLoaded(true);

            bukkitPlugin.onEnable();

            Mockito.verify(platformBootstrap).enable();
            Mockito.verify(pluginInstance).onEnable();
        }

    }

    @Nested
    class OnDisable {

        @Test
        void testFullDisable() {
            bukkitPlugin.setLoaded(true);

            bukkitPlugin.onDisable();

            Mockito.verify(platformBootstrap).disable();
            Mockito.verify(pluginInstance).onDisable();
        }

    }
}