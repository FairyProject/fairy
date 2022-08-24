package io.fairyproject.mc.event.world;

import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCWorld;

public class MCWorldUnloadEvent extends MCWorldEvent implements Cancellable {
    public MCWorldUnloadEvent(MCWorld world) {
        super(world);
    }
}
