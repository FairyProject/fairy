package io.fairyproject.mc.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import io.fairyproject.mc.util.Position;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
public class MCPlayerMoveEvent implements MCPlayerEvent, Cancellable {

    private final MCPlayer player;
    private final Position fromPos;
    private Position toPos;

    private boolean changed;

    public MCPlayerMoveEvent(MCPlayer player, Position fromPos, Position toPos) {
        this.player = player;
        this.fromPos = fromPos;
        this.toPos = toPos;
    }

    public void setToPos(Position pos) {
        this.toPos = pos;
        this.changed = true;
    }

}
