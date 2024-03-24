package io.fairyproject.bukkit.gui.pane.mapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RectanglePaneMapping implements PaneMapping {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private int[] cachedSlots;

    @Override
    public int getSize() {
        return width * height;
    }

    @Override
    public int[] getSlots() {
        if (cachedSlots == null) {
            cachedSlots = new int[width * height];
            for (int i = 0; i < cachedSlots.length; i++) {
                cachedSlots[i] = getSlot(i);
            }
        }
        return cachedSlots;
    }

    @Override
    public int getSlot(int index) {
        return x + index % width + (y + index / width) * 9;
    }

    @Override
    public int getIndex(int slot) {
        return (slot % 9 - x) + (slot / 9 - y) * width;
    }

    @Override
    public int getSlot(int x, int y) {
        return getSlot(x + y * width);
    }

    @Override
    public boolean hasAxis() {
        return true;
    }
}
