package io.fairyproject.mc;

import lombok.experimental.UtilityClass;

import java.util.UUID;

public interface MCEntity {

    static <T> MCEntity from(T world) {
        return MCEntity.Companion.BRIDGE.from(world);
    }

    MCWorld getWorld();

    UUID getUuid();

    int getId();

    <T> T as(Class<T> playerClass);

    @UtilityClass
    class Companion {
        public MCEntity.Bridge BRIDGE;
    }

    interface Bridge {

        MCEntity from(Object entity);

    }

}
