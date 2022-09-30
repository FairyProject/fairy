package io.fairyproject.mc.event.world;

import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.event.trait.MCWorldEvent;
import org.jetbrains.annotations.NotNull;

public class MCWorldUnloadEvent implements MCWorldEvent, Cancellable {

    private final MCWorld world;

    public MCWorldUnloadEvent(MCWorld world) {
        this.world = world;
    }

    @Override
    public @NotNull MCWorld getWorld() {
        return this.world;
    }
}
