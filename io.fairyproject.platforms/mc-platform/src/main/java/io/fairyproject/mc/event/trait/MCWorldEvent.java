package io.fairyproject.mc.event.trait;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCWorld;
import org.jetbrains.annotations.NotNull;

public interface MCWorldEvent extends Event {

    @NotNull MCWorld getWorld();

}
