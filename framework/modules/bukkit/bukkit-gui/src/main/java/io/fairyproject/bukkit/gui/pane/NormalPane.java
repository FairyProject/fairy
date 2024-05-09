package io.fairyproject.bukkit.gui.pane;

import io.fairyproject.bukkit.gui.pane.mapping.PaneMapping;
import io.fairyproject.bukkit.gui.slot.GuiSlot;

import java.util.HashMap;
import java.util.Map;

public class NormalPane extends AbstractPane implements MutablePane {

    private final Map<Integer, GuiSlot> guiSlots;

    public NormalPane(PaneMapping paneMapping) {
        super(paneMapping);
        this.guiSlots = new HashMap<>();
    }

    @Override
    public GuiSlot getSlot(int slot) {
        return this.guiSlots.getOrDefault(slot, null);
    }

    @Override
    public void setRawSlot(int pos, GuiSlot guiSlot) {
        this.guiSlots.put(pos, guiSlot);
    }

    public void clear() {
        this.guiSlots.clear();
    }
}
