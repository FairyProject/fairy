package io.fairyproject.mc.event.trait;

import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import org.jetbrains.annotations.NotNull;

public interface MCPlayerEvent extends MCEntityEvent {

    @Override
    default @NotNull MCEntity entity() {
        return this.player();
    }

    @NotNull MCPlayer player();

}
