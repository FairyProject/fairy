package io.fairyproject.mc.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Position;

public class MCPlayerTeleportEvent extends MCPlayerMoveEvent implements Cancellable {

    public MCPlayerTeleportEvent(MCPlayer player, Position fromPos, Position toPos) {
        super(player, fromPos, toPos);
    }

}
