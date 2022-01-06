package io.fairyproject.mc;

import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping;
import io.fairyproject.mc.protocol.netty.NettyInjector;

public interface MCInitializer {

    default void apply() {
        MCProtocol.initialize(this.createNettyInjector(), this.createProtocolMapping());
        MCServer.Companion.CURRENT = this.createMCServer();
        MCEntity.Companion.BRIDGE = this.createEntityBridge();
        MCWorld.Companion.BRIDGE = this.createWorldBridge();
        MCPlayer.Companion.BRIDGE = this.createPlayerBridge();
        MCGameProfile.Companion.BRIDGE = this.createGameProfileBridge();
    }

    NettyInjector createNettyInjector();
    MCProtocolMapping createProtocolMapping();
    MCServer createMCServer();
    MCEntity.Bridge createEntityBridge();
    MCWorld.Bridge createWorldBridge();
    MCPlayer.Bridge createPlayerBridge();
    MCGameProfile.Bridge createGameProfileBridge();

}
