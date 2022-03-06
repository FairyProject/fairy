package io.fairyproject.mc.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Pos;
import lombok.Getter;

@Event
@Getter
public class MCPlayerTeleportEvent extends MCPlayerMoveEvent implements Cancellable {

    public MCPlayerTeleportEvent(MCPlayer player, Pos fromPos, Pos toPos) {
        super(player, fromPos, toPos);
    }
}
