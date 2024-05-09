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

package io.fairyproject.devtools.bukkit.command;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.command.event.BukkitCommandContext;
import io.fairyproject.devtools.reload.Reloader;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DevToolCommandTest extends JUnitJupiterBase {

    private Reloader reloader;
    private DevToolCommand devToolCommand;

    @BeforeEach
    void setUp() {
        this.reloader = Mockito.mock(Reloader.class);
        this.devToolCommand = new DevToolCommand(reloader);
    }

    @Test
    void reload() {
        CommandSender commandSender = Mockito.mock(CommandSender.class);
        this.devToolCommand.reload(new BukkitCommandContext(commandSender, new String[0]));

        Mockito.verify(this.reloader).reload(FairyBukkitPlatform.INSTANCE.getMainPlugin());
    }

}