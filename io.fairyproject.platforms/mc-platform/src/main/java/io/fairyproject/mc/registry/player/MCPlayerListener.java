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

package io.fairyproject.mc.registry.player;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.event.NativePlayerLoginEvent;
import lombok.RequiredArgsConstructor;

@InjectableComponent
@RequiredArgsConstructor
public class MCPlayerListener {

    private final MCPlayerRegistry registry;
    private final MCPlayerPlatformOperator platformOperator;
    private final GlobalEventNode eventNode;

    @PostInitialize
    public void onPostInitialize() {
        this.eventNode.addListener(NativePlayerLoginEvent.class, this::onNativePlayerLogin);
        this.eventNode.addListener(MCPlayerQuitEvent.class, this::onPlayerQuit);
    }

    private void onNativePlayerLogin(NativePlayerLoginEvent event) {
        MCPlayer mcPlayer = this.platformOperator.create(
                event.getName(),
                event.getUuid(),
                event.getAddress()
        );
        Object nativePlayer = event.getNativePlayer();
        mcPlayer.setNative(nativePlayer);

        this.registry.addPlayer(mcPlayer);
    }

    public void onPlayerQuit(MCPlayerQuitEvent event) {
        MCPlayer player = event.getPlayer();

        this.registry.removePlayer(player.getUUID());
    }

}
