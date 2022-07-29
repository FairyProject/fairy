package io.fairyproject.mc.event;

import io.fairyproject.mc.MCPlayer;

public class MCPlayerQuitEvent extends MCPlayerEvent {
    public MCPlayerQuitEvent(MCPlayer player) {
        super(player);
    }
}
