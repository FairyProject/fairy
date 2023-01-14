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

package io.fairyproject.tests.mc.protocol;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.netty.NettyManager;
import io.fairyproject.FairyPlatform;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.plugin.Plugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockPacketEventsAPI extends PacketEventsAPI<Plugin> {

    private final MCServer mcServer;
    private final MCVersionMappingRegistry mappingRegistry;

    @Getter
    private boolean loaded = false;
    private MockServerManager serverManager;

    @Override
    public void load() {
        if (this.loaded) {
            return;
        }
        this.loaded = true;
        this.serverManager = new MockServerManager(mcServer, mappingRegistry);
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void terminate() {
        // do nothing
    }

    @Override
    public Plugin getPlugin() {
        return FairyPlatform.INSTANCE.getMainPlugin();
    }

    @Override
    public ServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public ProtocolManager getProtocolManager() {
        return null;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return null;
    }

    @Override
    public NettyManager getNettyManager() {
        return new MockNettyManager();
    }

    @Override
    public ChannelInjector getInjector() {
        return null;
    }
}
