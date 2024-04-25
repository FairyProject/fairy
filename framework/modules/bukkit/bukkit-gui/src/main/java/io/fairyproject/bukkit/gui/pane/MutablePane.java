package io.fairyproject.bukkit.gui.pane;

import io.fairyproject.bukkit.gui.slot.GuiSlot;

import java.util.ArrayList;
import java.util.List;

public interface MutablePane extends Pane {

    void setRawSlot(int pos, GuiSlot guiSlot);

    default void setSlot(int slot, GuiSlot guiSlot) {
        setRawSlot(getMapping().getSlot(slot), guiSlot);
    }

    default void setSlot(int x, int y, GuiSlot guiSlot) {
        setRawSlot(getMapping().getSlot(x, y), guiSlot);
    }

    default void setSlot(int startX, int startY, int endX, int endY, GuiSlot guiSlot) {
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                setSlot(x, y, guiSlot);
            }
        }
    }

    default void setSlot(int[] slots, GuiSlot guiSlot) {
        for (int slot : slots) {
            setSlot(slot, guiSlot);
        }
    }

    default List<Integer> findEmptySlots() {
        List<Integer> emptySlots = new ArrayList<>();

        for (int slot : getMapping().getSlots()) {
            if (getSlot(slot) == null) {
                emptySlots.add(slot);
            }
        }

        return emptySlots;
    }

    default void fillEmptySlots(GuiSlot guiSlot) {
        for (int slot : findEmptySlots()) {
            setRawSlot(slot, guiSlot);
        }
    }

}
