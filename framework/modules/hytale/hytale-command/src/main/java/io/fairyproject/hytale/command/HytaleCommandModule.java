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

package io.fairyproject.hytale.command;

import io.fairyproject.command.CommandService;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.hytale.command.presence.DefaultPresenceProvider;
import io.fairyproject.hytale.command.presence.PlayerPresenceProvider;
import lombok.RequiredArgsConstructor;

@InjectableComponent
@RequiredArgsConstructor
public class HytaleCommandModule {

    private final CommandService commandService;

    @PreInitialize
    public void onPreInitialize() {
        // Register presence provider for general commands (HytaleCommandContext)
        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());
        // Register presence provider for player commands (HytalePlayerCommandContext)
        commandService.registerDefaultPresenceProvider(new PlayerPresenceProvider());
    }
}
