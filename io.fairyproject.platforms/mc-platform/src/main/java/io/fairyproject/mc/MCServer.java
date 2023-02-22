package io.fairyproject.mc;

import io.fairyproject.mc.version.MCVersion;

import java.util.UUID;

public interface MCServer {

    static MCServer current() {
        return Companion.CURRENT;
    }

    boolean isMainThread();

    MCVersion getVersion();

    @Deprecated
    class Companion {

        public static MCServer CURRENT;

    }

}
