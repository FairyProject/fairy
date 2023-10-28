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

package io.fairyproject.bootstrap.app;

import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.PluginFileReader;
import io.fairyproject.bootstrap.instance.PluginInstance;
import io.fairyproject.bootstrap.platform.PlatformBootstrap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AppLauncherTest {

    private PluginFileReader pluginFileReader;
    private PluginInstance pluginInstance;
    private PlatformBootstrap platformBootstrap;
    private AppShutdownHookRegistry shutdownHookRegistry;
    private AppLauncher appLauncher;

    @BeforeEach
    void setUp() {
        pluginFileReader = Mockito.mock(PluginFileReader.class);
        pluginInstance = Mockito.mock(PluginInstance.class);
        platformBootstrap = Mockito.mock(PlatformBootstrap.class);
        shutdownHookRegistry = Mockito.mock(AppShutdownHookRegistry.class);
        appLauncher = new AppLauncher(
                pluginFileReader,
                pluginInstance,
                platformBootstrap,
                shutdownHookRegistry
        );
    }

    @Nested
    class OnLoad {

        @Test
        void preLoadFailed() {
            Mockito.when(platformBootstrap.preload()).thenReturn(false);

            assertFalse(appLauncher.start());
        }

        @Test
        void testFullLoad() {
            JsonObject jsonObject = new JsonObject();
            Mockito.when(platformBootstrap.preload()).thenReturn(true);
            Mockito.when(pluginFileReader.read(AppLauncher.class)).thenReturn(jsonObject);

            assertTrue(appLauncher.start());

            Mockito.verify(pluginInstance).init(jsonObject);
            Mockito.verify(platformBootstrap).load(Mockito.any());
            Mockito.verify(pluginInstance).onLoad();
            Mockito.verify(platformBootstrap).enable();
            Mockito.verify(pluginInstance).onEnable();
            Mockito.verify(shutdownHookRegistry).register();
        }

    }

}