package io.fairyproject.mc;

public interface MCInitializer {

    default void apply() {
        MCAdventure.initialize(this.createAdventure());
        MCServer.Companion.CURRENT = this.createMCServer();
        MCEntity.Companion.BRIDGE = this.createEntityBridge();
        MCWorld.Companion.BRIDGE = this.createWorldBridge();
        MCPlayer.Companion.BRIDGE = this.createPlayerBridge();
        MCGameProfile.Companion.BRIDGE = this.createGameProfileBridge();
    }

    MCAdventure.AdventureHook createAdventure();
    MCServer createMCServer();
    MCEntity.Bridge createEntityBridge();
    MCWorld.Bridge createWorldBridge();
    MCPlayer.Bridge createPlayerBridge();
    MCGameProfile.Bridge createGameProfileBridge();

}
