package io.fairyproject.bukkit.gui.pane.mapping;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

@RequiredArgsConstructor
public class StaticPaneMapping implements PaneMapping {

    private final int[] slots;

    @Override
    public int getSize() {
        return slots.length;
    }

    @Override
    public int[] getSlots() {
        return slots;
    }

    @Override
    public int getSlot(int index) {
        return slots[index];
    }

    @Override
    public int getIndex(int slot) {
        return ArrayUtils.indexOf(slots, slot);
    }

    @Override
    public int getSlot(int x, int y) {
        throw new UnsupportedOperationException("StaticPaneMapping doesn't have axis");
    }

    @Override
    public boolean hasAxis() {
        return false;
    }
}
