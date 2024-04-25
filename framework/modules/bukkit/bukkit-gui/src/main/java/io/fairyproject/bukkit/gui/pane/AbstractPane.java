package io.fairyproject.bukkit.gui.pane;

import io.fairyproject.bukkit.gui.pane.mapping.PaneMapping;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractPane implements Pane {

    protected final PaneMapping paneMapping;

    @Override
    public PaneMapping getMapping() {
        return paneMapping;
    }

    @Override
    public int[] getUsedSlots() {
        return paneMapping.getSlots();
    }

}
