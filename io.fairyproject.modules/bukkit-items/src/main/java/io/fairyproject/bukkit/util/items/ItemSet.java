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

package io.fairyproject.bukkit.util.items;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemSet {

    private static final int CONTENT_SIZE = 36;

    public static ItemSetBuilder builder(int slots) {
        return new ItemSetBuilder(slots);
    }

    public static ItemSetBuilder builderHotbar() {
        return new ItemSetBuilder(9);
    }

    public static ItemSetBuilder builderContents() {
        return new ItemSetBuilder(CONTENT_SIZE);
    }

    public static ItemSetBuilder builderArmorContents() {
        return new ItemSetBuilder.Armor();
    }

    public static ItemSetBuilder builderPlayerInventory() {
        return new ItemSetBuilder.PlayerInventory();
    }

    public static Slot createSlotBy(Object t) {
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
        this.slots[index] = ItemSet.createSlotBy(t);
    }

    public ItemStack[] toItems(Player player) {
        ItemStack[] itemStacks = new ItemStack[Math.max(this.getSlotCount(), 36)];
        for (int i = 0; i < itemStacks.length; i++) {
            final Slot slot = this.getSlot(i);
            if (slot != null) {
                itemStacks[i] = slot.getItem(player);
            }
        }
        return itemStacks;
    }

    public void apply(Player player) {
        ItemStack[] itemStacks = this.toItems(player);
        player.getInventory().setContents(itemStacks);
        player.updateInventory();
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

        protected ItemSet itemSet;

        private ItemSetBuilder() {

        }

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

        public static class Armor extends ItemSetBuilder {

            private Armor() {
                this.itemSet = new ItemSet.Armors();
            }

            public void set(ArmorPart part, ItemStack itemStack) {
                this.set(part.getSlot(), itemStack);
            }

            public void set(ArmorPart part, ImanityItem imanityItem) {
                this.set(part.getSlot(), imanityItem);
            }

            @Override
            public Armors build() {
                return (Armors) super.build();
            }
        }

        public static class PlayerInventory extends ItemSetBuilder {

            private PlayerInventory() {
                this.itemSet = new ItemSet.PlayerInventory();
            }

            public void set(ArmorPart part, ItemStack itemStack) {
                this.set(CONTENT_SIZE + part.getSlot(), itemStack);
            }

            public void set(ArmorPart part, ImanityItem imanityItem) {
                this.set(CONTENT_SIZE + part.getSlot(), imanityItem);
            }

            @Override
            public ItemSet.PlayerInventory build() {
                return (ItemSet.PlayerInventory) super.build();
            }

        }

    }

    public static class Armors extends ItemSet {

        public Armors() {
            super(4);
        }

        public ItemStack getItem(ArmorPart part, Player viewer) {
            return this.getItem(part.getSlot(), viewer);
        }

        public void setSlot(ArmorPart part, Object t) {
            this.setSlot(part.getSlot(), t);
        }

        @Override
        public ItemStack[] toItems(Player player) {
            ItemStack[] itemStacks = new ItemStack[Math.max(this.getSlotCount(), 4)];
            for (int i = 0; i < itemStacks.length; i++) {
                final Slot slot = this.getSlot(i);
                if (slot != null) {
                    itemStacks[i] = this.getItem(i, player);
                }
            }
            return itemStacks;
        }

        @Override
        public void apply(Player player) {
            ItemStack[] itemStacks = this.toItems(player);
            player.getInventory().setArmorContents(itemStacks);
            player.updateInventory();
        }
    }

    public static class PlayerInventory extends ItemSet {

        @Getter
        private final Armors armors;

        public PlayerInventory() {
            super(CONTENT_SIZE);
            this.armors = new Armors();
        }

        @Override
        public int getSlotCount() {
            return CONTENT_SIZE + 4;
        }

        @Override
        public Slot[] getSlots() {
            return ArrayUtils.addAll(super.getSlots(), this.armors.getSlots());
        }

        @Override
        public Slot getSlot(int index) {
            if (index >= CONTENT_SIZE) {
                return this.armors.getSlot(index - CONTENT_SIZE);
            }
            return super.getSlot(index);
        }

        @Override
        public void setSlot(int index, Object t) {
            if (index >= CONTENT_SIZE) {
                this.armors.setSlot(index - CONTENT_SIZE, t);
            }
            super.setSlot(index, t);
        }

        public ItemStack getItem(ArmorPart part, Player viewer) {
            return this.armors.getItem(part.getSlot(), viewer);
        }

        public void setSlot(ArmorPart part, Object t) {
            this.armors.setSlot(part.getSlot(), t);
        }

        @Override
        public ItemStack[] toItems(Player player) {
            ItemStack[] itemStacks = new ItemStack[Math.max(this.getSlotCount(), 40)];
            for (int i = 0; i < itemStacks.length; i++) {
                final Slot slot = this.getSlot(i);
                if (slot != null) {
                    itemStacks[i] = this.getItem(i, player);
                }
            }
            return itemStacks;
        }

        @Override
        public void apply(Player player) {
            ItemStack[] itemStacks = super.toItems(player);
            player.getInventory().setContents(itemStacks);
            this.armors.apply(player);
        }
    }

}
