package io.fairyproject.bukkit.gui.pane;

import io.fairyproject.bukkit.gui.pane.mapping.PaneMapping;
import io.fairyproject.bukkit.gui.slot.GuiSlot;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class PaginatedPane extends AbstractPane {

    private final List<GuiSlot> guiSlots;
    private int page;

    public PaginatedPane(PaneMapping paneMapping) {
        super(paneMapping);
        this.guiSlots = new ArrayList<>();
    }

    public void clear() {
        this.guiSlots.clear();
    }

    public void addSlot(GuiSlot guiSlot) {
        this.guiSlots.add(guiSlot);
    }

    @Override
    public GuiSlot getSlot(int slot) {
        int index = page * paneMapping.getSize() + this.slotToIndex(slot);
        if (index >= this.guiSlots.size())
            return null;

        return this.guiSlots.get(index);
    }

    private int slotToIndex(int slot) {
        // check slots int array and find it index
        int index = this.paneMapping.getIndex(slot);
        if (index == ArrayUtils.INDEX_NOT_FOUND) {
            throw new IllegalArgumentException("Slot " + slot + " is not in the slots array");
        }

        return index;
    }

    public int getPage() {
        return page;
    }

    public int getMaxPage() {
        return (int) Math.ceil((double) this.guiSlots.size() / this.paneMapping.getSize());
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void nextPage() {
        this.setPage(this.page + 1);
    }

    public void previousPage() {
        this.setPage(this.page - 1);
    }
}
