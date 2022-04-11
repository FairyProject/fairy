package io.fairyproject.mc;

import lombok.experimental.UtilityClass;

public interface MCWorld {

    static <T> MCWorld from(T world) {
        return Companion.BRIDGE.from(world);
    }

    <T> T as(Class<T> worldClass);

    int getMaxY();

    int getMaxSectionY();

    @UtilityClass
    class Companion {
        public Bridge BRIDGE;
    }

    interface Bridge {

        MCWorld from(Object world);

    }

}
