package io.fairyproject.mc;

import java.util.UUID;

public interface MCServer {

    static MCServer current() {
        return Companion.CURRENT;
    }

    MCEntity getEntity(UUID entityUuid);

    class Companion {

        public static MCServer CURRENT;

    }

}
