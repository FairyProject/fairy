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

import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.mock.MockPlugin;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BukkitReloadStartupHandlerTest {

    private PluginManager pluginManager;
    private BukkitReloadStartupHandler bukkitReloadStartupHandler;
    private MockPlugin fairyPlugin;
    private JavaPlugin javaPlugin;

    @BeforeEach
    void setUp() {
        pluginManager = Mockito.mock(PluginManager.class);
        Server server = Mockito.mock(Server.class);
        Mockito.when(server.getPluginManager()).thenReturn(pluginManager);

        bukkitReloadStartupHandler = new BukkitReloadStartupHandler(server);

        fairyPlugin = new MockPlugin();
        javaPlugin = Mockito.mock(JavaPlugin.class);

        JavaPluginUtil.setCurrentPlugin(javaPlugin);
    }

    @Test
    void startPluginMustNotBeNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> bukkitReloadStartupHandler.start(null));
    }

    @Test
    void testStart() {
        bukkitReloadStartupHandler.start(fairyPlugin);

        Mockito.verify(pluginManager).enablePlugin(javaPlugin);
        Mockito.verify(javaPlugin).onLoad();
    }
}