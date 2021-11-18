package io.fairyproject.mc.event;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;

@Event
public class MCPlayerQuitEvent extends MCPlayerEvent {
    public MCPlayerQuitEvent(MCPlayer player) {
        super(player);
    }
}
