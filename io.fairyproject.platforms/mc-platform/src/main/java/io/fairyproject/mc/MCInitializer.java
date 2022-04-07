package io.fairyproject.mc;

import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.container.ComponentRegistry;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.component.PacketListenerComponentHolder;
import io.fairyproject.mc.protocol.netty.NettyInjector;

public interface MCInitializer {

    default void apply() {
        MCProtocol.initialize(this.createPacketEvents());
        MCAdventure.initialize(this.createAdventure());
        MCServer.Companion.CURRENT = this.createMCServer();
        MCEntity.Companion.BRIDGE = this.createEntityBridge();
        MCWorld.Companion.BRIDGE = this.createWorldBridge();
        MCPlayer.Companion.BRIDGE = this.createPlayerBridge();
        MCGameProfile.Companion.BRIDGE = this.createGameProfileBridge();

        ComponentRegistry.registerComponentHolder(new PacketListenerComponentHolder());
    }

    PacketEventsAPI<?> createPacketEvents();
    MCAdventure.AdventureHook createAdventure();
    MCServer createMCServer();
    MCEntity.Bridge createEntityBridge();
    MCWorld.Bridge createWorldBridge();
    MCPlayer.Bridge createPlayerBridge();
    MCGameProfile.Bridge createGameProfileBridge();

}
