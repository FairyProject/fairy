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

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.MCVersion;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemSet implements Cloneable {

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

    public static ItemSetBuilder.PlayerInventory builderPlayerInventory() {
        return new ItemSetBuilder.PlayerInventory();
    }

    public static Slot createSlotBy(Object t) {
        if (t == null) {
            return null;
        }
        if (t instanceof Slot) {
            return (Slot) t;
        }
        if (t instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) t;
            // if it was fairy item, we convert the slot to fairy item slot with an amount
            FairyItem fairyItem = FairyItemRef.get(itemStack);
            if (fairyItem != null)
                return new FairyItemSlot(fairyItem, itemStack.getAmount(), itemStack.getDurability());

            return new ItemStackSlot(itemStack);
        }
        if (t instanceof FairyItem) {
            return new FairyItemSlot((FairyItem) t, 1, (short) 0);
        }
        throw new UnsupportedOperationException();
    }

    @Getter
    private final Slot[] slots;

    public ItemSet(int slotCount) {
        this.slots = new Slot[slotCount];
    }

    @Override
    public ItemSet clone() {
        ItemSet itemSet = new ItemSet(this.slots.length);
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] != null)
                itemSet.slots[i] = this.slots[i].clone();
        }

        return itemSet;
    }

    public int getSlotCount() {
        return this.slots.length;
    }

    public Slot getSlot(int index) {
        return this.slots[index];
    }

    public ItemStack getItem(int index, Player viewer) {
        Slot slot = this.getSlot(index);
        if (slot == null)
            return null;
        return slot.getItem(viewer);
    }

    public void setSlot(int index, Object t) {
        this.slots[index] = ItemSet.createSlotBy(t);
    }

    public ItemStack[] toItems(Player player) {
        ItemStack[] itemStacks = new ItemStack[Math.min(this.getSlotCount(), 36)];
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

    public interface Slot extends Cloneable {
        ItemStack getItem(Player player);

        Slot clone();
    }

    @Getter
    public static class ItemStackSlot implements Slot {

        private final ItemStack itemStack;

        public ItemStackSlot(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public ItemStack getItem(Player player) {
            return this.itemStack;
        }

        @Override
        public Slot clone() {
            return new ItemStackSlot(this.itemStack.clone());
        }
    }

    @Getter
    public static class FairyItemSlot implements Slot {

        private final FairyItem fairyItem;
        private final int amount;
        private final int durability;

        public FairyItemSlot(FairyItem fairyItem, int amount, int durability) {
            this.fairyItem = fairyItem;
            this.amount = amount;
            this.durability = durability;
        }

        @Override
        public ItemStack getItem(Player player) {
            return this.fairyItem.provide(MCPlayer.from(player))
                    .amount(this.amount)
                    .durability(this.durability)
                    .build();
        }

        @Override
        public Slot clone() {
            return new FairyItemSlot(this.fairyItem, this.amount, this.durability);
        }
    }

    public static class SlotEmpty implements Slot {

        @Override
        public ItemStack getItem(Player player) {
            return null;
        }

        @Override
        public Slot clone() {
            return new SlotEmpty();
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
                this.itemSet.setSlot(slot + i, item[i]);
            }
            return this;
        }

        public ItemSetBuilder set(int slot, FairyItem... item) {
            for (int i = 0; i < item.length; i++) {
                this.itemSet.setSlot(slot + i, item[i]);
            }
            return this;
        }

        public ItemSetBuilder set(int slot, Slot... slots) {
            for (int i = 0; i < slots.length; i++) {
                this.itemSet.setSlot(slot + i, slots[i]);
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

            public void set(ArmorPart part, FairyItem fairyItem) {
                this.set(part.getSlot(), fairyItem);
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

            public PlayerInventory set(ArmorPart part, ItemStack itemStack) {
                return this.set(CONTENT_SIZE + part.getSlot(), itemStack);
            }

            public PlayerInventory set(ArmorPart part, FairyItem fairyItem) {
                return this.set(CONTENT_SIZE + part.getSlot(), fairyItem);
            }

            @Override
            public PlayerInventory set(int slot, ItemStack... item) {
                return (PlayerInventory) super.set(slot, item);
            }

            @Override
            public PlayerInventory set(int slot, FairyItem... item) {
                return (PlayerInventory) super.set(slot, item);
            }

            @Override
            public ItemSetBuilder.PlayerInventory set(int slot, Slot... slots) {
                return (PlayerInventory) super.set(slot, slots);
            }

            @Override
            public PlayerInventory setEmpty(int slot) {
                return (PlayerInventory) super.setEmpty(slot);
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

        @Override
        public ItemSet.Armors clone() {
            Armors armors = new Armors();
            for (int i = 0; i < this.getSlotCount(); i++) {
                Slot slot = this.getSlot(i);
                if (slot != null) {
                    armors.setSlot(i, slot.clone());
                }
            }
            return armors;
        }
    }

    public static class PlayerInventory extends ItemSet {

        @Getter
        private final Armors armors;
        private Slot offhand;

        public PlayerInventory() {
            super(CONTENT_SIZE);
            this.armors = new Armors();
            this.offhand = new SlotEmpty();
        }

        @Override
        public int getSlotCount() {
            return CONTENT_SIZE + 4;
        }

        @Override
        public Slot[] getSlots() {
            List<Slot> slots = new ArrayList<>();
            slots.addAll(Arrays.asList(super.getSlots()));
            slots.addAll(Arrays.asList(this.armors.getSlots()));
            slots.add(this.offhand);
            return slots.toArray(new Slot[0]);
        }

        @Override
        public Slot getSlot(int index) {
            if (index >= CONTENT_SIZE + 4) {
                return this.getOffhand();
            }
            if (index >= CONTENT_SIZE) {
                return this.armors.getSlot(index - CONTENT_SIZE);
            }
            return super.getSlot(index);
        }

        @Override
        public void setSlot(int index, Object t) {
            if (index >= CONTENT_SIZE + this.armors.getSlotCount()) {
                this.setOffhand(t);
                return;
            }
            if (index >= CONTENT_SIZE) {
                this.armors.setSlot(index - CONTENT_SIZE, t);
                return;
            }
            super.setSlot(index, t);
        }

        public ItemStack getItem(ArmorPart part, Player viewer) {
            return this.armors.getItem(part.getSlot(), viewer);
        }

        public void setSlot(ArmorPart part, Object t) {
            this.armors.setSlot(part.getSlot(), t);
        }

        public Slot getOffhand() {
            return this.offhand;
        }

        public void setOffhand(Slot slot) {
            this.offhand = slot;
        }

        public void setOffhand(Object t) {
            this.offhand = ItemSet.createSlotBy(t);
        }

        @Override
        public ItemStack[] toItems(Player player) {
            ItemStack[] itemStacks = new ItemStack[Math.max(this.getSlotCount(), 36 + 4 + 1)]; // 36 = player inventory, 4 = armor, 1 = offhand
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
            // offhand support
            if (this.offhand != null && MCServer.current().getVersion().isOrAbove(MCVersion.V1_9)) {
                player.getInventory().setItemInOffHand(this.offhand.getItem(player));
            }
        }

        @Override
        public ItemSet.PlayerInventory clone() {
            ItemSet.PlayerInventory playerInventory = new PlayerInventory();
            for (int i = 0; i < this.getSlotCount(); i++) {
                Slot slot = this.getSlot(i);
                if (slot != null) {
                    playerInventory.setSlot(i, slot.clone());
                }
            }
            for (int i = 0; i < this.armors.getSlotCount(); i++) {
                Slot slot = this.armors.getSlot(i);
                if (slot != null) {
                    playerInventory.setSlot(CONTENT_SIZE + i, slot.clone());
                }
            }
            if (offhand != null) {
                playerInventory.setOffhand(offhand.clone());
            }

            return playerInventory;
        }
    }

}
