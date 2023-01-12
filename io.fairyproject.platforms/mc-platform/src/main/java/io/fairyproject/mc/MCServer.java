package io.fairyproject.mc;

import io.fairyproject.mc.version.MCVersion;

import java.util.UUID;

public interface MCServer {

    static MCServer current() {
        return Companion.CURRENT;
    }

    MCEntity getEntity(UUID entityUuid);

    boolean isMainThread();

    MCVersion getVersion();

    class Companion {

        public static MCServer CURRENT;

    }

}
