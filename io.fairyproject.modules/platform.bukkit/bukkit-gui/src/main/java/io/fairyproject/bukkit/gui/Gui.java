package io.fairyproject.bukkit.gui;

import io.fairyproject.bukkit.gui.event.GuiCloseEvent;
import io.fairyproject.bukkit.gui.event.GuiOpenEvent;
import io.fairyproject.bukkit.gui.pane.Pane;
import io.fairyproject.bukkit.gui.slot.GuiSlot;
import io.fairyproject.bukkit.events.BukkitEventFilter;
import io.fairyproject.bukkit.events.BukkitEventNode;
import io.fairyproject.event.EventNode;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.scheduler.MCSchedulers;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.util.ConditionUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Gui {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    @Getter
    private final int id;
    @Getter
    private final MetadataMap metadataMap;
    private final BukkitEventNode bukkitEventNode;
    private final Map<Integer, EventNode<InventoryEvent>> slotEventNodes;
    private final List<Consumer<Player>> openCallbacks;
    private final List<Consumer<Player>> drawCallbacks;
    private final List<Consumer<Player>> closeCallbacks;
    private final List<Pane> panes;
    private final boolean[] usedSlots;
    private final Component title;

    @Nullable
    private Inventory inventory;
    private GuiSlot[] guiSlots;
    @Getter
    private EventNode<Event> eventNode;
    private int maxSlots;

    public Gui(BukkitEventNode bukkitEventNode, Component title) {
        this.id = ID_COUNTER.getAndIncrement();
        this.bukkitEventNode = bukkitEventNode;
        this.openCallbacks = new ArrayList<>();
        this.drawCallbacks = new ArrayList<>();
        this.closeCallbacks = new ArrayList<>();
        this.metadataMap = MetadataMap.create();
        this.slotEventNodes = new HashMap<>();
        this.title = title;
        this.panes = new ArrayList<>();
        this.usedSlots = new boolean[9 * 6];
        this.maxSlots = -1;
    }

    public void openOrUpdate(Player player) {
        if (this.isOpening()) {
            this.update(player);
        } else {
            this.open(player);
        }
    }

    @SuppressWarnings("deprecation")
    public void open(Player player) {
        if (this.isOpening())
            return;

        MCPlayer mcPlayer = MCPlayer.from(player);
        GuiOpenEvent event = new GuiOpenEvent(player, this);
        event.call();
        if (event.isCancelled())
            return;

        this.inventory = Bukkit.createInventory(null, this.getRows() * 9, MCAdventure.asLegacyString(this.title, mcPlayer.getLocale()));
        this.guiSlots = new GuiSlot[this.maxSlots + 1];
        this.registerListeners();

        this.update(player);
        player.openInventory(this.inventory);

        this.onOpen(player);
    }

    public void update(Player player) {
        if (!this.isOpening())
            return;

        this.drawCallbacks.forEach(callback -> callback.accept(player));

        for (Pane pane : this.panes) {
            this.renderPane(pane);
        }

        this.renderSlots(player);
    }

    private void close(Player player) {
        Arrays.fill(this.usedSlots, false);
        if (this.eventNode != null)
            this.eventNode.closeAndReportException();
        this.eventNode = null;
        this.inventory = null;
        this.guiSlots = null;

        new GuiCloseEvent(player, this).call();

        // For some reason when you try to open an inventory immediately after closing one, it makes bukkit events not being called...
        MCSchedulers.getGlobalScheduler().schedule(() -> this.onClose(player), 1L);
    }

    public void onOpenCallback(Consumer<Player> callback) {
        this.openCallbacks.add(callback);
    }

    public void onDrawCallback(Consumer<Player> callback) {
        this.drawCallbacks.add(callback);
    }

    public void onCloseCallback(Consumer<Player> callback) {
        this.closeCallbacks.add(callback);
    }

    public boolean isInventory(Inventory inventory) {
        return Objects.equals(this.inventory, inventory);
    }

    public @Nullable ItemStack getItem(int slot) {
        if (this.inventory == null)
            return null;

        return this.inventory.getItem(slot);
    }

    public <T extends GuiSlot> void forEachGuiSlots(Class<T> aClass, BiConsumer<Integer, T> callback) {
        if (this.guiSlots == null)
            return;

        for (int i = 0; i < this.guiSlots.length; i++) {
            GuiSlot guiSlot = this.guiSlots[i];
            if (guiSlot == null)
                continue;

            if (aClass.isInstance(guiSlot))
                callback.accept(i, aClass.cast(guiSlot));
        }
    }

    public void addPane(Pane pane) {
        this.panes.add(pane);
        for (int slot : pane.getUsedSlots()) {
            ConditionUtils.is(slot >= 0 && slot < 9 * 6, "Slot " + slot + " is not in the inventory");
            ConditionUtils.is(!this.usedSlots[slot], "Slot " + slot + " is already used");
            this.usedSlots[slot] = true;

            if (slot > this.maxSlots)
                this.maxSlots = slot;
        }
    }

    public <T extends Pane> T getPane(Class<T> clazz) {
        for (Pane pane : this.panes) {
            if (clazz.isInstance(pane))
                return clazz.cast(pane);
        }
        return null;
    }

    public void updateSlot(@NotNull Player player, int slot, @Nullable GuiSlot guiSlot) {
        ConditionUtils.notNull(inventory, "Inventory is null");

        ItemStack previous = this.inventory.getItem(slot);
        ItemStack current = null;
        if (guiSlot != null) {
            current = guiSlot.getItemStack(player, this);
        }

        if (previous != null && previous.equals(current)) {
            return;
        }

        inventory.setItem(slot, current);
    }

    public int getRows() {
        return (int) Math.ceil(this.maxSlots / 9.0);
    }

    private void renderSlots(Player player) {
        if (this.inventory == null)
            return;

        for (int i = 0; i < guiSlots.length; i++) {
            GuiSlot guiSlot = guiSlots[i];
            updateSlot(player, i, guiSlot);
        }
    }

    private void renderPane(Pane pane) {
        for (int slot : pane.getUsedSlots()) {
            GuiSlot guiSlot = pane.getSlot(slot);
            if (guiSlot == null) {
                if (guiSlots[slot] != null) {
                    this.onSlotRemoved(slot);
                }
                guiSlots[slot] = null;
                continue;
            }

            if (guiSlots[slot] != null) {
                this.onSlotRemoved(slot);
            }

            guiSlots[slot] = guiSlot;
            this.onSlotAdd(slot, guiSlot);
        }
    }

    private void onSlotAdd(int slot, GuiSlot guiSlot) {
        EventNode<InventoryEvent> eventNode = guiSlot.getEventNode(this);
        if (eventNode != null) {
            this.slotEventNodes.put(slot, eventNode);
            this.eventNode.addChild(eventNode);
        }
    }

    private void onSlotRemoved(int slot) {
        EventNode<InventoryEvent> eventNode = this.slotEventNodes.remove(slot);
        if (eventNode != null) {
            eventNode.closeAndReportException();
        }
    }

    private void registerListeners() {
        this.eventNode = EventNode.create(String.format("fairy:gui-%d", this.id), BukkitEventFilter.ALL, null);
        this.eventNode.addListener(GuiOpenEvent.class, this::onGuiOpen);
        this.eventNode.addListener(PlayerQuitEvent.class, this::onPlayerQuit);
        this.eventNode.addListener(InventoryClickEvent.class, this::onInventoryClick);
        this.eventNode.addListener(InventoryDragEvent.class, this::onInventoryDrag);
        this.eventNode.addListener(InventoryCloseEvent.class, this::onInventoryClose);

        this.bukkitEventNode.addChild(this.eventNode);
    }

    private void onGuiOpen(@NotNull GuiOpenEvent event) {
        Gui gui = event.getGui();
        if (gui == this)
            return;

        if (inventory == null)
            return;

        Player player = event.getPlayer();
        if (!inventory.getViewers().contains(player))
            return;

        // close current gui if another gui is opened
        this.close(player);
    }

    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        assert this.inventory != null;

        Player player = event.getPlayer();
        if (!this.inventory.getViewers().contains(player))
            return;

        this.close(player);
    }

    private void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!this.isInventory(event.getInventory()))
            return;

        this.close(player);
    }

    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!this.isInventory(event.getClickedInventory())) {
            switch (event.getAction()) {
                case COLLECT_TO_CURSOR:
                case MOVE_TO_OTHER_INVENTORY:
                case UNKNOWN:
                    event.setCancelled(true);
                    break;
            }
            return;
        }

        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot < 0 || slot >= this.usedSlots.length)
            return;

        GuiSlot guiSlot = guiSlots[slot];
        if (guiSlot == null)
            return;

        guiSlot.onInventoryClick(event, this);
    }

    private void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (!this.isInventory(event.getInventory()))
            return;

        InventoryView view = event.getView();
        for (int slot : event.getRawSlots()) {
            Inventory currentInventory = getInventory(view, slot);
            if (!this.isInventory(currentInventory))
                return;

            GuiSlot guiSlot = guiSlots[slot];
            if (guiSlot == null) {
                event.setCancelled(true);
                return;
            }

            if (guiSlot.onInventoryDrag(event, this))
                event.setCancelled(true);
        }
    }

    public final Inventory getInventory(InventoryView view, int rawSlot) {
        // Slot may be -1 if not properly detected due to client bug
        // e.g. dropping an item into part of the enchantment list section of an enchanting table
        if (rawSlot == -1) {
            return null;
        }

        if (rawSlot < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }

    private void onOpen(Player player) {
        this.openCallbacks.forEach(callback -> callback.accept(player));
    }

    private void onClose(Player player) {
        this.closeCallbacks.forEach(callback -> callback.accept(player));
    }

    public boolean isOpening() {
        return this.inventory != null;
    }

}
