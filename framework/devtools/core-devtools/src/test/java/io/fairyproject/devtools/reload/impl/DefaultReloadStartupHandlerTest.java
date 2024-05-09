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
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.singleton.SingletonObjectRegistry;
import io.fairyproject.container.object.singleton.SingletonObjectRegistryImpl;
import io.fairyproject.mock.MockPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class DefaultReloadStartupHandlerTest {

    private ContainerNode node;
    private ContainerContext context;
    private SingletonObjectRegistry singletonObjectRegistry;
    private MockPlugin plugin;
    private DefaultReloadStartupHandler reloadStartupHandler;

    @BeforeEach
    void setUp() {
        node = Mockito.mock(ContainerNode.class);
        context = Mockito.mock(ContainerContext.class);

        singletonObjectRegistry = new SingletonObjectRegistryImpl();
        Mockito.doReturn(singletonObjectRegistry).when(context).singletonObjectRegistry();

        plugin = new MockPlugin();
        plugin.setNode(node);

        reloadStartupHandler = new DefaultReloadStartupHandler(context);
    }

    @Test
    void startPluginMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> reloadStartupHandler.start(null));
    }

    @Test
    void startShouldCallPluginEnable() {
        reloadStartupHandler.start(plugin);

        assertTrue(plugin.isOnInitialCalled());
        assertTrue(plugin.isOnPreEnableCalled());
        assertTrue(plugin.isOnPostEnableCalled());
    }

    @Test
    void startShouldLoadNodeFromContext() {
        Mockito.doReturn(true).when(context).loadContainerNode(node);

        reloadStartupHandler.start(plugin);

        Mockito.verify(context).loadContainerNode(node);
    }

    @Test
    void startShouldRegisterPluginAsSingleton() {
        reloadStartupHandler.start(plugin);

        assertTrue(singletonObjectRegistry.containsSingleton(MockPlugin.class));
        assertSame(singletonObjectRegistry.getSingleton(MockPlugin.class), this.plugin);
        assertEquals(singletonObjectRegistry.getSingletonLifeCycle(MockPlugin.class), LifeCycle.CONSTRUCT);
    }

}