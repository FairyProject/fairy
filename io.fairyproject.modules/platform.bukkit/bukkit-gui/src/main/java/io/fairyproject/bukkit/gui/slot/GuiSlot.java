package io.fairyproject.bukkit.gui.slot;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.bukkit.gui.Gui;
import io.fairyproject.bukkit.gui.pane.PaginatedPane;
import io.fairyproject.bukkit.gui.pane.Pane;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import io.fairyproject.event.EventNode;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface GuiSlot {

    static GuiSlot of(@NotNull ItemStack itemStack) {
        return new StaticGuiSlot(itemStack, null);
    }

    static GuiSlot of(@NotNull XMaterial material) {
        return new StaticGuiSlot(material.parseItem(), null);
    }

    static GuiSlot of(@NotNull ItemBuilder itemBuilder) {
        return new StaticGuiSlot(itemBuilder.build(), null);
    }

    static GuiSlot of(@NotNull XMaterial material, Component displayName) {
        return new StaticGuiSlot(ItemBuilder.of(material).name(displayName).build(), null);
    }

    static GuiSlot of(@NotNull XMaterial material, String displayName) {
        return new StaticGuiSlot(ItemBuilder.of(material).name(displayName).build(), null);
    }

    static GuiSlot of(@NotNull ItemStack itemStack, @NotNull Consumer<Player> clickCallback) {
        return new StaticGuiSlot(itemStack, event -> clickCallback.accept((Player) event.getWhoClicked()));
    }

    static GuiSlot of(@NotNull XMaterial material, @NotNull Consumer<Player> clickCallback) {
        return new StaticGuiSlot(material.parseItem(), event -> clickCallback.accept((Player) event.getWhoClicked()));
    }

    static GuiSlot of(@NotNull ItemBuilder itemBuilder, @NotNull Consumer<Player> clickCallback) {
        return new StaticGuiSlot(itemBuilder.build(), event -> clickCallback.accept((Player) event.getWhoClicked()));
    }

    static GuiSlot of(@NotNull ItemStack itemStack, @NotNull BiConsumer<Player, ClickType> clickCallback) {
        return new StaticGuiSlot(itemStack, event -> clickCallback.accept((Player) event.getWhoClicked(), event.getClick()));
    }

    static GuiSlot of(@NotNull XMaterial material, @NotNull BiConsumer<Player, ClickType> clickCallback) {
        return new StaticGuiSlot(material.parseItem(), event -> clickCallback.accept((Player) event.getWhoClicked(), event.getClick()));
    }

    static GuiSlot of(@NotNull ItemBuilder itemBuilder, @NotNull BiConsumer<Player, ClickType> clickCallback) {
        return new StaticGuiSlot(itemBuilder.build(), event -> clickCallback.accept((Player) event.getWhoClicked(), event.getClick()));
    }

    static ItemSelectorGuiSlot.Builder itemSelector(Pane pane, int slot) {
        return new ItemSelectorGuiSlot.Builder(pane, slot);
    }

    static ItemSelectorGuiSlot.Builder itemSelector(Pane pane, int x, int y) {
        return new ItemSelectorGuiSlot.Builder(pane, x, y);
    }

    static GuiSlot nextPage(PaginatedPane pane, ItemStack itemStack) {
        return new ModPageGuiSlot(pane, itemStack, 1, null);
    }

    static GuiSlot previousPage(PaginatedPane pane, ItemStack itemStack) {
        return new ModPageGuiSlot(pane, itemStack, -1, null);
    }

    static GuiSlot nextPage(PaginatedPane pane) {
        return new ModPageGuiSlot(pane, ItemBuilder.of(XMaterial.ARROW).name("&aNext Page").build(), 1, null);
    }

    static GuiSlot previousPage(PaginatedPane pane) {
        return new ModPageGuiSlot(pane, ItemBuilder.of(XMaterial.ARROW).name("&aPrevious Page").build(), -1, null);
    }

    static GuiSlot modPage(PaginatedPane pane, ItemStack itemStack, int mod, @Nullable BiConsumer<Player, ClickType> clickCallback) {
        return new ModPageGuiSlot(pane, itemStack, mod, clickCallback != null ? event -> clickCallback.accept((Player) event.getWhoClicked(), event.getClick()) : null);
    }

    ItemStack getItemStack(@NotNull Player player, @NotNull Gui gui);

    default void update(@NotNull Player player, int slot, @NotNull Gui gui) {
        gui.updateSlot(player, slot, this);
    }

    default void onInventoryClick(@NotNull InventoryClickEvent event, @NotNull Gui gui) {
        // do nothing
    }

    /**
     * called on inventory drag event occurs
     *
     * @param event the event
     * @param gui  the gui
     * @return true if the event should be cancelled
     */
    default boolean onInventoryDrag(@NotNull InventoryDragEvent event, @NotNull Gui gui) {
        return true;
    }

    @Nullable default EventNode<InventoryEvent> getEventNode(Gui gui) {
        return null;
    }

}
