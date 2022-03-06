package io.fairyproject.mc.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Pos;
import lombok.Getter;

@Event
@Getter
public class MCPlayerMoveEvent extends MCPlayerEvent implements Cancellable {

    private final Pos fromPos;
    private Pos toPos;

    private boolean changed;

    public MCPlayerMoveEvent(MCPlayer player, Pos fromPos, Pos toPos) {
        super(player);
        this.fromPos = fromPos;
        this.toPos = toPos;
    }

    public void setToPos(Pos pos) {
        this.toPos = pos;
        this.changed = true;
    }

}
