package io.fairyproject.bukkit.gui.pane;

import io.fairyproject.bukkit.gui.pane.mapping.PaneMapping;
import io.fairyproject.bukkit.gui.slot.GuiSlot;

public interface Pane {

    static NormalPane normal(int[] slots) {
        return new NormalPane(PaneMapping.staticMapping(slots));
    }

    static NormalPane normal(int x, int y, int width, int height) {
        return new NormalPane(PaneMapping.rectangle(x, y, width, height));
    }

    static NormalPane normal(int width, int height) {
        return new NormalPane(PaneMapping.rectangle(width, height));
    }

    static NormalPane normal(int rows) {
        return new NormalPane(PaneMapping.rectangle(rows));
    }

    static NormalPane normal(PaneMapping mapping) {
        return new NormalPane(mapping);
    }

    static PaginatedPane paginated(int[] slots) {
        return new PaginatedPane(PaneMapping.staticMapping(slots));
    }

    static PaginatedPane paginated(int x, int y, int width, int height) {
        return new PaginatedPane(PaneMapping.rectangle(x, y, width, height));
    }

    static PaginatedPane paginated(int width, int height) {
        return new PaginatedPane(PaneMapping.rectangle(width, height));
    }

    static PaginatedPane paginated(int rows) {
        return new PaginatedPane(PaneMapping.rectangle(rows));
    }

    static PaginatedPane paginated(PaneMapping mapping) {
        return new PaginatedPane(mapping);
    }

    PaneMapping getMapping();

    int[] getUsedSlots();

    GuiSlot getSlot(int slot);

}
