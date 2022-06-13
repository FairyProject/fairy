package io.fairyproject.mc.event;

import io.fairyproject.mc.MCPlayer;

public class MCPlayerJoinEvent extends MCPlayerEvent {
    public MCPlayerJoinEvent(MCPlayer player) {
        super(player);
    }
}
