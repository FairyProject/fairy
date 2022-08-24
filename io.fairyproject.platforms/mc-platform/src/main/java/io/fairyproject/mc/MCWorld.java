package io.fairyproject.mc;

import io.fairyproject.mc.util.AudienceProxy;
import io.fairyproject.metadata.MetadataMap;
import lombok.experimental.UtilityClass;

import java.util.List;

public interface MCWorld extends AudienceProxy {

    static <T> MCWorld from(T world) {
        return Companion.BRIDGE.from(world);
    }

    static MCWorld getByName(String name) {
        return Companion.BRIDGE.getByName(name);
    }

    static List<MCWorld> all() {
        return Companion.BRIDGE.all();
    }

    <T> T as(Class<T> worldClass);

    int getMaxY();

    int getMaxSectionY();

    String name();

    MetadataMap metadata();

    List<MCPlayer> players();

    @UtilityClass
    class Companion {
        public Bridge BRIDGE;
    }

    interface Bridge {

        MCWorld from(Object world);

        MCWorld getByName(String name);

        List<MCWorld> all();

    }

}
