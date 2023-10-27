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

package io.fairyproject.command;

import io.fairyproject.bukkit.command.BukkitCommandListener;
import io.fairyproject.bukkit.command.map.BukkitCommandMap;
import io.fairyproject.bukkit.command.map.FakeBukkitCommandMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitCommandListenerTest {

    private BukkitCommandMap bukkitCommandMap;
    private BukkitCommandListener bukkitCommandListener;
    private BaseCommand baseCommand;

    @BeforeEach
    void setUp() {
        bukkitCommandMap = new FakeBukkitCommandMap();
        bukkitCommandListener = new BukkitCommandListener(bukkitCommandMap);

        baseCommand = Mockito.mock(BaseCommand.class);
    }

    @Test
    void onCommandInitial() {
        bukkitCommandListener.onCommandInitial(baseCommand, new String[0]);

        assertTrue(bukkitCommandMap.isRegistered(baseCommand));
    }

    @Test
    void onCommandRemoval() {
        bukkitCommandMap.register(baseCommand);
        bukkitCommandListener.onCommandRemoval(baseCommand);

        assertFalse(bukkitCommandMap.isRegistered(baseCommand));
    }

}