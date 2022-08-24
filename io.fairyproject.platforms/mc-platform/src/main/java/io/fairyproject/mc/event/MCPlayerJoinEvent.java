package io.fairyproject.mc.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCPlayer;

public class MCPlayerJoinEvent extends MCPlayerEvent implements Cancellable {
    public MCPlayerJoinEvent(MCPlayer player) {
        super(player);
    }
}
