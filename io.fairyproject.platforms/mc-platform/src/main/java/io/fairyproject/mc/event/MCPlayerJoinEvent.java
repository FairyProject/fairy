package io.fairyproject.mc.event;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;

@Event
public class MCPlayerJoinEvent extends MCPlayerEvent {
    public MCPlayerJoinEvent(MCPlayer player) {
        super(player);
    }
}
