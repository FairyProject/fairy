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

package io.fairyproject.devtools.reload.impl;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.singleton.SingletonObjectRegistry;
import io.fairyproject.devtools.reload.ReloadStartupHandler;
import io.fairyproject.plugin.Plugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultReloadStartupHandler implements ReloadStartupHandler {

    private final ContainerContext context;

    @Override
    public void start(Plugin plugin) {
        if (plugin == null)
            throw new IllegalArgumentException("Plugin must not be null");

        plugin.onInitial();
        plugin.onPreEnable();

        // register plugin as singleton
        // TODO possibility to recreate instance of the plugin?
        SingletonObjectRegistry singletonObjectRegistry = context.singletonObjectRegistry();
        singletonObjectRegistry.registerSingleton(plugin.getClass(), plugin);
        singletonObjectRegistry.setSingletonLifeCycle(plugin.getClass(), LifeCycle.CONSTRUCT);

        context.loadContainerNode(plugin.getNode());

        plugin.onPluginEnable();
    }
}
