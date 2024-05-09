package io.fairyproject.mc.nametag;

import io.fairyproject.event.Cancellable;
import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NameTagUpdateEvent implements Event, Cancellable {

    private final MCPlayer player;
    private final MCPlayer target;
    private NameTag nameTag;

}
