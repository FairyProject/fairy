package io.fairyproject.bukkit.gui.pane.mapping;

public interface PaneMapping {

    static PaneMapping rectangle(int x, int y, int width, int height) {
        return new RectanglePaneMapping(x, y, width, height);
    }

    static PaneMapping rectangle(int width, int height) {
        return new RectanglePaneMapping(0, 0, width, height);
    }

    static PaneMapping rectangle(int rows) {
        return new RectanglePaneMapping(0, 0, 9, rows);
    }

    static PaneMapping staticMapping(int[] slots) {
        return new StaticPaneMapping(slots);
    }

    static PaneMapping outline(int x, int y, int width, int height) {
        return new OutlinePaneMapping(x, y, width, height);
    }

    static PaneMapping outline(int width, int height) {
        return new OutlinePaneMapping(0, 0, width, height);
    }

    static PaneMapping outline(int rows) {
        return new OutlinePaneMapping(0, 0, 9, rows);
    }

    int getSize();

    int[] getSlots();

    int getSlot(int index);

    int getIndex(int slot);

    /**
     * Get the slot index from the x and y coordinates
     * Only works if the mapping has axis
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the slot index
     * @throws UnsupportedOperationException if the mapping doesn't have axis
     */
    int getSlot(int x, int y);

    boolean hasAxis();

}
