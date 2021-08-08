/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.fairy.bukkit.util.items;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemSet {

    public Slot createSlotBy(Object t) {
        if (t == null) {
            return null;
        }
        if (t instanceof ItemStack) {
            return new SlotItemStack((ItemStack) t);
        }
        if (t instanceof ImanityItem) {
            return new SlotImanityItem((ImanityItem) t);
        }
        throw new UnsupportedOperationException();
    }

    @Getter
    private final Slot[] slots;

    public ItemSet(int slotCount) {
        this.slots = new Slot[slotCount];
    }

    public int getSlotCount() {
        return this.slots.length;
    }

    public Slot getSlot(int index) {
        return this.slots[index];
    }

    public ItemStack getItem(int index, Player viewer) {
        return this.getSlot(index).getItem(viewer);
    }

    public void setSlot(int index, Object t) {
        this.slots[index] = this.createSlotBy(t);
    }

    public interface Slot {
        ItemStack getItem(Player player);
    }

    @Getter
    public static class SlotItemStack implements Slot {

        private final ItemStack itemStack;
        public SlotItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public ItemStack getItem(Player player) {
            return this.itemStack;
        }
    }

    @Getter
    public static class SlotImanityItem implements Slot {

        private final ImanityItem imanityItem;
        public SlotImanityItem(ImanityItem imanityItem) {
            this.imanityItem = imanityItem;
        }

        @Override
        public ItemStack getItem(Player player) {
            return this.imanityItem.get(player);
        }
    }

    public static class SlotEmpty implements Slot {

        @Override
        public ItemStack getItem(Player player) {
            return null;
        }
    }

    public static class ItemSetBuilder {

        private final ItemSet itemSet;

        private ItemSetBuilder(int slotCount) {
            this.itemSet = new ItemSet(slotCount);
        }

        public ItemSetBuilder set(int slot, ItemStack... item) {
            for (int i = 0; i < item.length; i++) {
                this.itemSet.setSlot(slot + i, item);
            }
            return this;
        }

        public ItemSetBuilder set(int slot, ImanityItem... item) {
            for (int i = 0; i < item.length; i++) {
                this.itemSet.setSlot(slot + i, item);
            }
            return this;
        }

        public ItemSetBuilder setEmpty(int slot) {
            this.itemSet.setSlot(slot, null);
            return this;
        }

        public ItemSet build() {
            return this.itemSet;
        }

    }

}
