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
import io.fairyproject.mock.MockPlugin;
import io.fairyproject.plugin.PluginDescription;
import org.bukkit.Server;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

class BukkitReloadStartupHandlerTest {

    private PluginManager pluginManager;
    private BukkitReloadStartupHandler bukkitReloadStartupHandler;
    private MockPlugin fairyPlugin;
    private JavaPlugin javaPlugin;
    private be.seeseemelk.mockbukkit.MockPlugin newJavaPlugin;

    @BeforeEach
    void setUp() throws InvalidPluginException, InvalidDescriptionException {
        pluginManager = Mockito.mock(PluginManager.class);
        Server server = Mockito.mock(Server.class);
        Mockito.when(server.getPluginManager()).thenReturn(pluginManager);

        bukkitReloadStartupHandler = new BukkitReloadStartupHandler(server);

        fairyPlugin = new MockPlugin();
        MockBukkit.getOrCreateMock();
        javaPlugin = MockBukkit.createMockPlugin("a");
        newJavaPlugin = Mockito.spy(MockBukkit.createMockPlugin("b"));

        Mockito.when(pluginManager.loadPlugin(Mockito.any(File.class))).thenReturn(newJavaPlugin);

        RootJavaPluginIdentifier.getInstance().addFirst(new SpecifyJavaPluginIdentifier(javaPlugin));
    }

    @Test
    void startPluginMustNotBeNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> bukkitReloadStartupHandler.start(null));
    }

    @Test
    void testStart() throws URISyntaxException, InvalidPluginException, InvalidDescriptionException {
        bukkitReloadStartupHandler.start(fairyPlugin);

        URL url = javaPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.toURI());
        Mockito.verify(pluginManager).loadPlugin(file);
        Mockito.verify(newJavaPlugin).onLoad();
        Mockito.verify(pluginManager).enablePlugin(newJavaPlugin);
    }
}