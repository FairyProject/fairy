package io.fairyproject.mc.protocol.impl.mock;

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
