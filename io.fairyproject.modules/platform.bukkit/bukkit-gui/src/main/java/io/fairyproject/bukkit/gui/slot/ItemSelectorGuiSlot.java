package io.fairyproject.bukkit.gui.slot;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.bukkit.gui.Gui;
import io.fairyproject.bukkit.gui.pane.Pane;
import io.fairyproject.bukkit.events.BukkitEventFilter;
import io.fairyproject.event.EventNode;
import io.fairyproject.mc.scheduler.MCSchedulers;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * inspired by <a href="https://github.com/hamza-cskn/obliviate-invs/blob/master/advancedslot/src/main/java/mc/obliviate/inventory/advancedslot/AdvancedSlotManager.java">obliviate-invs</a>
 */
@AllArgsConstructor
public class ItemSelectorGuiSlot implements GuiSlot {

    private final int slot;
    private final ItemStack emptyItemStack;
    private final BiPredicate<InventoryClickEvent, ItemStack> canPickupPredicate;
    private final BiPredicate<InventoryClickEvent, ItemStack> canPlacePredicate;
    private final BiConsumer<InventoryClickEvent, ItemStack> pickupCallback;
    private final BiConsumer<InventoryClickEvent, ItemStack> placeCallback;
    private final BiConsumer<InventoryClickEvent, ItemStack> updateCallback;

    private ItemStack currentItemStack;

    public void update(@NotNull Player player, @NotNull Gui gui) {
        update(player, this.slot, gui);
    }

    public boolean isEmpty(Gui gui) {
        ItemStack itemStack = gui.getItem(this.slot);
        return !isItemNotEmpty(itemStack) || itemStack.isSimilar(emptyItemStack);
    }

    @Override
    public ItemStack getItemStack(@NotNull Player player, @NotNull Gui gui) {
        if (this.isItemNotEmpty(this.currentItemStack))
            return this.currentItemStack;

        return this.emptyItemStack;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onInventoryClick(@NotNull InventoryClickEvent event, @NotNull Gui gui) {
        Player player = (Player) event.getWhoClicked();
        if (this.isEmpty(gui)) {
            handleEmptyInventoryClick(player, gui, event);
        } else {
            handleNotEmptyInventoryClick(event, gui, player);
        }
    }

    private void handleNotEmptyInventoryClick(@NotNull InventoryClickEvent event, @NotNull Gui gui, Player player) {
        InventoryAction action = event.getAction();
        if (this.checkPrePickup(event, action))
            return;

        // pre put action
        if (this.checkPrePlace(event, action))
            return;

        event.setCancelled(false);

        //general checks
        switch (event.getAction()) {
            //case COLLECT_TO_CURSOR:
            case PICKUP_ONE:
            case DROP_ONE_SLOT:
                onPickup(event, getCopyOfItemWithAmount(event.getCurrentItem(), 1));
                this.onUpdate(event);
                break;
            case PICKUP_HALF:
                final int amount = event.getCurrentItem().getAmount() / 2 + (event.getCurrentItem().getAmount() % 2 == 0 ? 0 : 1);
                onPickup(event, getCopyOfItemWithAmount(event.getCurrentItem(), amount));
                this.onUpdate(event);
                break;
            //case PICKUP_SOME:
            case MOVE_TO_OTHER_INVENTORY:
            case PICKUP_ALL:
            case DROP_ALL_SLOT:
                onPickup(event, event.getCurrentItem());
                this.onUpdate(event);
                break;
            case SWAP_WITH_CURSOR:
                onPickup(event, event.getCurrentItem());
                onPlace(event, event.getCursor());
                this.onUpdate(event);
                break;
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                onPickup(event, event.getCurrentItem());
                ItemStack hotbarItem = getItemStackFromHotKey(event);
                if (hotbarItem != null)
                    onPlace(event, hotbarItem);
                this.onUpdate(event);
                break;
            case PLACE_ONE:
                onPlace(event, getCopyOfItemWithAmount(event.getCursor(), 1));
                this.onUpdate(event);
                break;
            case PLACE_SOME:
                onPlace(event, getCopyOfItemWithAmount(event.getCursor(), event.getCursor().getMaxStackSize()));
                this.onUpdate(event);
                break;
            case PLACE_ALL:
                onPlace(event, event.getCursor());
                this.onUpdate(event);
                break;
            default:
                return;
        }

        MCSchedulers.getGlobalScheduler().schedule(() -> {
            if (isEmpty(gui)) {
                gui.updateSlot(player, slot, this);
            }
        }, 1L);
    }

    private void handleEmptyInventoryClick(Player player, Gui gui, InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (isItemNotEmpty(cursor)) {
            if (!this.canPlace(event, cursor))
                return;

            ItemStack newCursor = null;
            if (event.isRightClick()) {
                if (cursor.getAmount() > 1) {
                    newCursor = getCopyOfItemWithAmount(cursor, cursor.getAmount() - 1);
                }
                cursor.setAmount(1);
            }
            event.setCursor(newCursor);
            this.onPlace(event, cursor);
            this.onUpdate(event);
            this.update(player, gui);
            return;
        }

        if (!event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD))
            return;

        ItemStack hotbarItem = getItemStackFromHotKey(event);
        assert hotbarItem != null;

        if (!this.canPlace(event, hotbarItem))
            return;

        this.onPlace(event, hotbarItem);
        this.onUpdate(event);
        this.update(player, gui);
    }

    @Override
    public @Nullable EventNode<InventoryEvent> getEventNode(Gui gui) {
        EventNode<InventoryEvent> eventNode = EventNode.value("advanced-gui-slot", BukkitEventFilter.INVENTORY, gui::isInventory);
        eventNode.addListener(InventoryClickEvent.class, event -> onInventoryClick(gui, event));

        return eventNode;
    }

    private void onInventoryClick(Gui gui, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryAction action = event.getAction();
        if (!action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
            return;

        if (event.getRawSlot() == event.getSlot())
            return;

        ItemStack clickedItemStack = event.getCurrentItem();
        if (!isItemNotEmpty(clickedItemStack))
            return;

        ItemStack itemStack = gui.getItem(slot);

        if (this.isEmpty(gui)) {
            if (!this.canPlace(event, clickedItemStack))
                return;

            event.setCurrentItem(null);
            this.onPlace(event, clickedItemStack);
            this.onUpdate(event);
            this.update(player, gui);
        } else if (itemStack != null && compareSimilar(itemStack, clickedItemStack) && itemStack.getAmount() < itemStack.getType().getMaxStackSize()) {
            if (!this.canPlace(event, clickedItemStack))
                return;

            int maxSize = itemStack.getType().getMaxStackSize();

            int transferSIze;
            if (itemStack.getAmount() + clickedItemStack.getAmount() > maxSize)
                transferSIze = maxSize - itemStack.getAmount();
            else
                transferSIze = clickedItemStack.getAmount();

            ItemStack transferItemStack = getCopyOfItemWithAmount(clickedItemStack, transferSIze);
            clickedItemStack.setAmount(clickedItemStack.getAmount() - transferSIze);
            if (clickedItemStack.getAmount() == 0)
                event.setCurrentItem(null);

            event.setCurrentItem(clickedItemStack);
            this.onPlace(event, transferItemStack);
            this.onUpdate(event);
            this.update(player, gui);
        }
    }

    private boolean canPickup(InventoryClickEvent event, ItemStack itemStack) {
        return canPickupPredicate == null || canPickupPredicate.test(event, itemStack);
    }

    private boolean canPlace(InventoryClickEvent event, ItemStack itemStack) {
        return canPlacePredicate == null || canPlacePredicate.test(event, itemStack);
    }

    private void onPlace(InventoryClickEvent event, ItemStack itemStack) {
        if (this.currentItemStack == null) {
            this.currentItemStack = itemStack;
        } else
            this.currentItemStack.setAmount(this.currentItemStack.getAmount() + itemStack.getAmount());

        if (placeCallback != null)
            placeCallback.accept(event, itemStack);

        if (updateCallback != null)
            updateCallback.accept(event, this.currentItemStack);
    }

    private void onPickup(InventoryClickEvent event, ItemStack itemStack) {
        if (currentItemStack == null) {
            return;
        }

        int amount = this.currentItemStack.getAmount() - itemStack.getAmount();
        if (amount == 0)
            this.currentItemStack = null;
        else
            this.currentItemStack.setAmount(amount);

        if (pickupCallback != null)
            pickupCallback.accept(event, itemStack);
    }

    private void onUpdate(InventoryClickEvent event) {
        if (updateCallback != null)
            updateCallback.accept(event, currentItemStack);
    }

    private boolean checkPrePlace(@NotNull InventoryClickEvent event, InventoryAction action) {
        switch (action) {
            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
                ItemStack hotbarItemStack = getItemStackFromHotKey(event);
                if (isItemNotEmpty(hotbarItemStack) && !canPlace(event, hotbarItemStack))
                    return true;
                break;

            case SWAP_WITH_CURSOR:
                ItemStack cursorItemStack = event.getCursor();
                if (isItemNotEmpty(cursorItemStack) && !canPlace(event, cursorItemStack))
                    return true;
                break;

            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                if (!canPlace(event, event.getCursor()))
                    return true;
                break;
        }

        return false;
    }

    private boolean checkPrePickup(@NotNull InventoryClickEvent event, InventoryAction action) {
        switch (action) {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                break;
            default:
                if (!this.canPickup(event, event.getCurrentItem()))
                    return true;
        }
        return false;
    }

    private boolean compareSimilar(final ItemStack item1, final ItemStack item2) {
        boolean aNotEmpty = isItemNotEmpty(item1);
        boolean bNotEmpty = isItemNotEmpty(item2);
        if (aNotEmpty && bNotEmpty)
            return item1.isSimilar(item2);
        else
            return !aNotEmpty && !bNotEmpty;
    }

    private ItemStack getCopyOfItemWithAmount(ItemStack item, int amount) {
        ItemStack result = item.clone();
        result.setAmount(amount);
        return result;
    }

    private boolean isItemEmpty(ItemStack itemStack) {
        return itemStack == null || XMaterial.AIR.isSimilar(itemStack) || itemStack.isSimilar(this.emptyItemStack);
    }

    private boolean isItemNotEmpty(ItemStack itemStack) {
        return !isItemEmpty(itemStack);
    }

    private ItemStack getItemStackFromHotKey(InventoryClickEvent event) {
        if (event.getHotbarButton() == -1)
            return null;
        return event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
    }

    public static class Builder {
        private final int slot;
        private ItemStack emptyItemStack = new ItemStack(Material.AIR);
        private ItemStack currentItemStack;
        private BiPredicate<InventoryClickEvent, ItemStack> canPickupPredicate;
        private BiPredicate<InventoryClickEvent, ItemStack> canPlacePredicate;
        private BiConsumer<InventoryClickEvent, ItemStack> pickupCallback;
        private BiConsumer<InventoryClickEvent, ItemStack> placeCallback;
        private BiConsumer<InventoryClickEvent, ItemStack> updateCallback;

        public Builder(Pane pane, int slot) {
            this.slot = pane.getMapping().getSlot(slot);
        }

        public Builder(Pane pane, int x, int y) {
            this.slot = pane.getMapping().getSlot(x, y);
        }

        public Builder emptyItem(ItemStack emptyItemStack) {
            this.emptyItemStack = emptyItemStack;
            return this;
        }

        public Builder currentItem(ItemStack currentItemStack) {
            this.currentItemStack = currentItemStack;
            return this;
        }

        public Builder canPickupPredicate(BiPredicate<InventoryClickEvent, ItemStack> prePickupPredicate) {
            this.canPickupPredicate = prePickupPredicate;
            return this;
        }

        public Builder canPlacePredicate(BiPredicate<InventoryClickEvent, ItemStack> prePutPredicate) {
            this.canPlacePredicate = prePutPredicate;
            return this;
        }

        public Builder pickupCallback(BiConsumer<InventoryClickEvent, ItemStack> pickupCallback) {
            this.pickupCallback = pickupCallback;
            return this;
        }

        public Builder placeCallback(BiConsumer<InventoryClickEvent, ItemStack> putCallback) {
            this.placeCallback = putCallback;
            return this;
        }

        public Builder updateCallback(BiConsumer<InventoryClickEvent, ItemStack> updateCallback) {
            this.updateCallback = updateCallback;
            return this;
        }

        public Builder canPickupPredicate(Predicate<ItemStack> prePickupPredicate) {
            this.canPickupPredicate = (event, itemStack) -> prePickupPredicate.test(itemStack);
            return this;
        }

        public Builder canPlacePredicate(Predicate<ItemStack> prePutPredicate) {
            this.canPlacePredicate = (event, itemStack) -> prePutPredicate.test(itemStack);
            return this;
        }

        public Builder pickupCallback(Consumer<ItemStack> pickupCallback) {
            this.pickupCallback = (event, itemStack) -> pickupCallback.accept(itemStack);
            return this;
        }

        public Builder placeCallback(Consumer<ItemStack> putCallback) {
            this.placeCallback = (event, itemStack) -> putCallback.accept(itemStack);
            return this;
        }

        public Builder updateCallback(Consumer<ItemStack> updateCallback) {
            this.updateCallback = (event, itemStack) -> updateCallback.accept(itemStack);
            return this;
        }

        public ItemSelectorGuiSlot build() {
            return new ItemSelectorGuiSlot(slot, emptyItemStack, canPickupPredicate, canPlacePredicate, pickupCallback, placeCallback, updateCallback, currentItemStack);
        }
    }
}
