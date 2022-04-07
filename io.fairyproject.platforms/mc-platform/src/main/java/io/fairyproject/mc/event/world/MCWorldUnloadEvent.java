package io.fairyproject.mc.event.world;

import io.fairyproject.event.Cancellable;
import io.fairyproject.event.Event;
import io.fairyproject.mc.MCWorld;

@Event
public class MCWorldUnloadEvent extends MCWorldEvent implements Cancellable {
    public MCWorldUnloadEvent(MCWorld world) {
        super(world);
    }
}
