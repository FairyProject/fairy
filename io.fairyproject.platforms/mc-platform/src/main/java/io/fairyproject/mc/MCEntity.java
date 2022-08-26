package io.fairyproject.mc;

import io.fairyproject.event.EventNode;
import io.fairyproject.mc.event.trait.MCEntityEvent;
import io.fairyproject.mc.util.Pos;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MCEntity {

    static <T> MCEntity from(T world) {
        return MCEntity.Companion.BRIDGE.from(world);
    }

    MCWorld getWorld();

    UUID getUUID();

    Pos pos();

    int getId();

    @NotNull EventNode<MCEntityEvent> eventNode();

    boolean teleport(Pos pos);

    <T> T as(Class<T> entityClass);

    @UtilityClass
    class Companion {
        public MCEntity.Bridge BRIDGE;
    }

    interface Bridge {

        MCEntity from(Object entity);

        int newEntityId();

    }

}
