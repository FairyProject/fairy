package io.fairyproject.bukkit.gui.pane.mapping;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@RequiredArgsConstructor
public class OutlinePaneMapping implements PaneMapping {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private int[] cachedSlots;
    private BitSet slotSet;

    @Override
    public int getSize() {
        cache();
        return cachedSlots.length;
    }

    private void cache() {
        if (cachedSlots == null) {
            List<Integer> slots = new ArrayList<>();
            slotSet = new BitSet(width * height);

            int index = 0;
            for (int i = 0; i < width; i++) {
                slots.add(getMenuSlot(i, 0));
            }

            for (int i = 1; i < height - 1; i++) {
                slots.add(getMenuSlot(0, i));
                slots.add(getMenuSlot(width - 1, i));
            }

            for (int i = 0; i < width; i++) {
                slots.add(getMenuSlot(i, height - 1));
            }

            cachedSlots = new int[slots.size()];
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);

                cachedSlots[i] = slot;
                slotSet.set(slot);
            }
        }
    }

    public int getMenuSlot(int index) {
        return x + index % width + (y + index / width) * 9;
    }

    public int getMenuSlot(int x, int y) {
        return getMenuSlot(x + y * width);
    }

    @Override
    public int[] getSlots() {
        cache();
        return cachedSlots;
    }

    @Override
    public int getSlot(int index) {
        cache();
        return cachedSlots[index];
    }

    @Override
    public int getIndex(int slot) {
        cache();
        return slotSet.get(slot) ? ArrayUtils.indexOf(cachedSlots, slot) : -1;
    }

    @Override
    public int getSlot(int x, int y) {
        int slot = getMenuSlot(x, y);
        cache();
        if (slotSet.get(slot)) {
            return slot;
        } else {
            throw new IllegalArgumentException("Slot " + x + ", " + y + " is not in the slots array");
        }
    }

    @Override
    public boolean hasAxis() {
        return true;
    }
}
