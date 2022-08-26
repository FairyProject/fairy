package io.fairyproject.mc.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import io.fairyproject.mc.util.Pos;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class MCPlayerMoveEvent implements MCPlayerEvent, Cancellable {

    private final MCPlayer player;
    private final Pos fromPos;
    private Pos toPos;

    private boolean changed;

    public MCPlayerMoveEvent(MCPlayer player, Pos fromPos, Pos toPos) {
        this.player = player;
        this.fromPos = fromPos;
        this.toPos = toPos;
    }

    public void setToPos(Pos pos) {
        this.toPos = pos;
        this.changed = true;
    }

}
