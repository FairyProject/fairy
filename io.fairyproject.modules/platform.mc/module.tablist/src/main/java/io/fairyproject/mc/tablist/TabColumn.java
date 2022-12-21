/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.mc.tablist;

import io.fairyproject.mc.MCPlayer;
import lombok.Getter;
import io.fairyproject.mc.protocol.MCVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum TabColumn {

    LEFT(0, "Left", -2, 1, 3),
    MIDDLE(1, "Middle", -1, 21, 3),
    RIGHT(2, "Right", 0, 41, 3),
    /**
     * Far Right Column
     * Note: This Column is only visible on 1.8+ player versions.
     */
    FAR_RIGHT(3, "Far-Right", 60, 61, 1);

    private final int startNumber;
    private final int incrementBy;
    private final int rawStart;
    private final List<Integer> numbers = new ArrayList<>();
    private final String identifier;
    private final int ordinal;

    TabColumn(int ordinal, String identifier, int rawStart, int startNumber, int incrementBy) {
        this.ordinal = ordinal;
        this.identifier = identifier;
        this.rawStart = rawStart;
        this.startNumber = startNumber;
        this.incrementBy = incrementBy;
        generate();
    }

    public static TabColumn getColumn(String identifier) {
        for (TabColumn tabColumn : TabColumn.values()) {
            if (tabColumn.getIdentifier().equalsIgnoreCase(identifier)) {
                return tabColumn;
            }
        }
        return null;
    }

    private void generate() {
        for (int i = 1; i <= 20; i++) {
            Integer numb = rawStart + (i * incrementBy);
            this.numbers.add(numb);
        }
    }

    public static TabColumn getFromSlot(MCPlayer player, Integer slot) {
        /* Player Version 1.7 */
        if (player.getVersion() == MCVersion.V1_7) {
            return Arrays.stream(TabColumn.values())
                    .filter(tabColumn -> tabColumn.getNumbers().contains(slot))
                    .findFirst().get();
            /* Player Version 1.8+ */
        } else {
            /* Left Column */
            if (isBetween(slot, 1, 20)) return LEFT;
            /* Middle Column */
            if (isBetween(slot, 21, 40)) return MIDDLE;
            /* Right Column */
            if (isBetween(slot, 41, 60)) return RIGHT;
            /* Far Right Column */
            if (isBetween(slot, 61, 80)) return FAR_RIGHT;
            return null;
        }
    }

    public int getNumber(MCPlayer player, int raw) {
        /* Check if the Player is not a 1.7 User */
        if (player.getVersion() != MCVersion.V1_7) {
            return raw - startNumber + 1;
        }
        int number = 0;
        for (int integer : numbers) {
            number++;
            if (integer == raw) {
                return number;
            }
        }
        return number;
    }

    public static TabColumn getFromOrdinal(int ordinal) {
        for (TabColumn column : TabColumn.values()) {
            if (column.getOrdinal() == ordinal) {
                return column;
            }
        }
        return null;
    }

    private static boolean isBetween(int base, int from, int to) {
        return from <= base && base <= to;
    }

}

