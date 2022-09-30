package io.fairyproject.mc;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import io.fairyproject.event.EventNode;
import io.fairyproject.mc.event.trait.MCEntityEvent;
import io.fairyproject.mc.util.Position;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface MCEntity {

    static <T> MCEntity from(T world) {
        return MCEntity.Companion.BRIDGE.from(world);
    }

    MCWorld getWorld();

    UUID getUUID();

    Position getPosition();

    int getId();

    @NotNull EventNode<MCEntityEvent> getEventNode();

    boolean teleport(Position pos);

    <T> T as(Class<T> entityClass);

    @NotNull List<EntityData> data();

    @UtilityClass
    class Companion {
        public MCEntity.Bridge BRIDGE;
    }

    interface Bridge {

        MCEntity from(Object entity);

        int newEntityId();

    }

}
