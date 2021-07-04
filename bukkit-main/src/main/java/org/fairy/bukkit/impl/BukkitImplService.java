/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.fairy.bukkit.impl;

import org.fairy.bean.*;
import org.fairy.bukkit.command.presence.DefaultPresenceProvider;
import org.fairy.bukkit.impl.server.ServerImplementation;
import org.fairy.command.CommandService;
import org.fairy.bukkit.Imanity;

@Service(name = "bukkit-impl", dependencies = "command")
public class BukkitImplService {

    private final BeanContext beanContext;

    @BeanConstructor
    public BukkitImplService(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @PreInitialize
    public void preInit() {
        CommandService commandService = (CommandService) this.beanContext.getBean(CommandService.class);
        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());
        Imanity.IMPLEMENTATION = ServerImplementation.load(this.beanContext);
    }

}
