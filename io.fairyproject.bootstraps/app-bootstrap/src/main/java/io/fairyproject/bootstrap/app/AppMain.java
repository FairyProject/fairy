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

import io.fairyproject.bootstrap.PluginClassInitializerFinder;
import io.fairyproject.bootstrap.PluginFileReader;
import io.fairyproject.bootstrap.platform.PlatformBootstrap;
import io.fairyproject.internal.FairyInternalIdentityMeta;
import io.fairyproject.plugin.initializer.PluginClassInitializer;

@FairyInternalIdentityMeta
public class AppMain {

    public static void main(String[] args) {
        PluginFileReader pluginFileReader = new PluginFileReader();
        PlatformBootstrap bootstrap = new AppBootstrap();
        PluginClassInitializer initializer = PluginClassInitializerFinder.find();
        ApplicationInstance instance = new ApplicationInstance(initializer, bootstrap);
        AppShutdownHookRegistry shutdownHookRegistry = new AppShutdownHookRegistry(bootstrap::disable);

        new AppLauncher(
                pluginFileReader,
                instance,
                bootstrap,
                shutdownHookRegistry
        ).start();
    }

}
