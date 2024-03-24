package io.fairyproject.bukkit.gui.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GuiUtils {

    public static int[] getSlots(int x, int y, int width, int height) {
        int[] slots = new int[width * height];
        int index = 0;
        for (int y1 = y; y1 < height; y1++) {
            for (int x1 = x; x1 < width; x1++) {
                slots[index++] = x1 + y1 * 9;
            }
        }
        return slots;
    }

    public static int[] getSlots(int width, int height) {
        return getSlots(0, 0, width, height);
    }

    public static int[] getSlots(int rows) {
        return getSlots(0, 0, 9, rows);
    }

}
