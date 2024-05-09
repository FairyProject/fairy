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
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.FairyPlatform;
import io.fairyproject.mock.MockPlugin;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.tests.bukkit.BukkitTestingHandle;
import io.fairyproject.tests.bukkit.FairyBukkitTestingPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DevToolTestingHandle implements BukkitTestingHandle {
    @Override
    public @NotNull ServerMock createServerMock() {
        return MockBukkit.getOrCreateMock();
    }

    @Override
    public Plugin plugin() {
        return new MockPlugin();
    }

    @Override
    public FairyPlatform platform() {
        return new FairyBukkitTestingPlatform();
    }

    @Override
    public @Nullable String scanPath() {
        return "testenv.io.fairyproject";
    }
}
