package io.fairyproject.mc.map;

import io.fairyproject.mc.MCPlayer;

public interface MapAdapter {

    Framebuffer render(MCPlayer mcPlayer);

    int ticks();

    default int priority() {
        return 0;
    }

}
