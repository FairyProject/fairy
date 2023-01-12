package io.fairyproject.mc;

import io.fairyproject.mc.version.MCVersionMappingRegistry;

public interface MCInitializer {

    default void apply(MCVersionMappingRegistry versionMappingRegistry) {
        MCServer.Companion.CURRENT = this.createMCServer();

        this.serverLoaded();
        MCAdventure.initialize(MCServer.current(), versionMappingRegistry, this.createAdventure());
        MCEntity.Companion.BRIDGE = this.createEntityBridge();
        MCWorld.Companion.BRIDGE = this.createWorldBridge();
        MCPlayer.Companion.BRIDGE = this.createPlayerBridge(versionMappingRegistry);
        MCGameProfile.Companion.BRIDGE = this.createGameProfileBridge();
    }

    void serverLoaded();

    MCAdventure.AdventureHook createAdventure();
    MCServer createMCServer();
    MCEntity.Bridge createEntityBridge();
    MCWorld.Bridge createWorldBridge();
    MCPlayer.Bridge createPlayerBridge(MCVersionMappingRegistry versionMappingRegistry);
    MCGameProfile.Bridge createGameProfileBridge();

}
