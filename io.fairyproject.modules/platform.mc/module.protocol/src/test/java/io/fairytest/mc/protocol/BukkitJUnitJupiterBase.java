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

package io.fairytest.mc.protocol;

import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.tests.TestingHandle;
import io.fairyproject.tests.base.JUnitJupiterBase;
import io.fairyproject.tests.bukkit.MockBukkitContext;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BukkitJUnitJupiterBase extends JUnitJupiterBase {

    private final AtomicBoolean mockBukkitInitialized = new AtomicBoolean(false);
    protected ServerMock server;
    protected MockPlugin plugin;

    @BeforeEach
    public void setupMockBukkit() {
        if (!mockBukkitInitialized.compareAndSet(false, true)) {
            return;
        }
        this.initMockBukkit();
    }

    private void initMockBukkit() {
        MockBukkitContext.get().initialize();
        this.server = MockBukkitContext.get().getServer();
        this.plugin = MockBukkitContext.get().getPlugin();
    }

    @Override
    public void initRuntime(TestingHandle testingHandle) {
        this.initMockBukkit();
        super.initRuntime(testingHandle);
    }

}
