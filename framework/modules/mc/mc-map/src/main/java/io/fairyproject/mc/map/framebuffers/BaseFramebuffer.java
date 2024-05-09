package io.fairyproject.mc.map.framebuffers;

import io.fairyproject.mc.map.Framebuffer;
import io.fairyproject.mc.map.packet.MapIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseFramebuffer implements Framebuffer {

    private final List<MapIcon> icons = new ArrayList<>();

    @Override
    public Collection<MapIcon> icons() {
        return this.icons;
    }

    public void addIcon(MapIcon icon) {
        this.icons.add(icon);
    }
}
