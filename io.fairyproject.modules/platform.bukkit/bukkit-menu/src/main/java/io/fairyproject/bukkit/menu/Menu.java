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

package io.fairyproject.bukkit.menu;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fairyproject.bukkit.events.BukkitEventFilter;
import io.fairyproject.bukkit.events.BukkitEventNode;
import io.fairyproject.bukkit.menu.event.ButtonClickEvent;
import io.fairyproject.bukkit.menu.event.MenuCloseEvent;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.container.Autowired;
import io.fairyproject.event.EventNode;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.task.Task;
import io.fairyproject.util.CC;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
@Setter
public abstract class Menu implements TerminableConsumer {

    private static final MetadataKey<Menu> METADATA = MetadataKey.create("fairy:menu", Menu.class);
    private static final Map<Class<? extends Menu>, List<Menu>> MENU_BY_TYPE = new ConcurrentHashMap<>();

    @Deprecated
    @Autowired
    private static BukkitEventNode bukkitEventNode;

    private final CompositeTerminable compositeTerminable;
    @Getter
    private final EventNode<Event> eventNode;
    private final Map<Integer, Button> buttonsMap;

    protected Player player;
    private Inventory inventory;

    private boolean opening;
    private boolean rendering;
    private boolean rerendering;
    private boolean reopening;
    private long openMillis;
    private long lastAccessMillis;
    private int updateCount;

    private Button placeholderButton = Button.placeholder(XMaterial.GRAY_STAINED_GLASS_PANE, " ");

    public Menu() {
        this.compositeTerminable = CompositeTerminable.create();
        this.buttonsMap = new HashMap<>();

        this.eventNode = EventNode.create("fairy:menu", BukkitEventFilter.ALL, null);
        this.eventNode.addListener(PlayerQuitEvent.class, this::onPlayerQuit);
        this.eventNode.addListener(PlayerDeathEvent.class, this::onPlayerDeath);
        this.eventNode.addListener(InventoryClickEvent.class, this::onInventoryClick);
        this.eventNode.addListener(InventoryCloseEvent.class, this::onInventoryClose);

        this.eventNode.bindWith(this);
    }

    private void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != this.player)
            return;

        if (this.isRerendering())
            return;

        this.remove();
    }

    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this.player)
            return;

        int slot = event.getSlot();
        boolean shiftClick = event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT;

        if (slot != event.getRawSlot()) {
            if (shiftClick)
                event.setCancelled(true);
            return;
        }

        Button button = this.buttonsMap.getOrDefault(slot, null);
        if (button == null) {
            if (shiftClick)
                event.setCancelled(true);
            return;
        }

        boolean cancel = button.shouldCancel(player, slot, event.getClick());
        if (!cancel && shiftClick) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null)
                player.getInventory().addItem(event.getCurrentItem());
        } else {
            event.setCancelled(cancel);
        }

        button.clicked(player, slot, event.getClick(), event.getHotbarButton());
        ButtonClickEvent buttonClickEvent = new ButtonClickEvent(
                player,
                this,
                button,
                slot
        );
        buttonClickEvent.call();

        if (!this.opening)
            return;

        this.lastAccessMillis = System.currentTimeMillis();

        if (event.isCancelled())
            Task.runMainLater(player::updateInventory, 1L);
    }

    private void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player != this.player)
            return;

        player.closeInventory();
        this.remove();
    }

    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != this.player)
            return;

        player.closeInventory();
        this.remove();
    }

    @NotNull
    @Override
    public <T extends Terminable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    protected Menu set(int slot, Button... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            this.set(slot + i, buttons[i]);
        }
        return this;
    }

    protected Menu set(int slot, Button button) {
        final int limit = this.getSizeLimit();
        Preconditions.checkArgument(slot < limit, "slot >= limit(" + limit + ")");
        this.buttonsMap.put(slot, button);
        return this;
    }

    protected Menu set(int x, int y, Button... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            this.set(getSlot(x, y) + i, buttons[i]);
        }
        return this;
    }

    protected int findFirstEmpty() {
        int limit = this.getSizeLimit();

        for (int i = 0; i < limit - 1; i++) {
            if (this.isEmpty(i)) {
                return i;
            }
        }

        return -1;
    }

    protected int findLastEmpty() {
        int limit = this.getSizeLimit();

        for (int i = limit - 1; i > 0; i++) {
            if (this.isEmpty(i)) {
                return i;
            }
        }

        return -1;
    }

    protected int findFirstEmptyBetween(int minSlot, int maxSlot) {
        int limit = this.getSizeLimit();
        Preconditions.checkArgument(maxSlot < limit, "maxSlot >= limit(" + limit + ")");
        Preconditions.checkArgument(minSlot >= 0, "minSlot < 0");

        for (int i = minSlot; i < maxSlot; i++) {
            if (this.isEmpty(i)) {
                return i;
            }
        }

        return -1;
    }

    protected int findLastEmptyBetween(int minSlot, int maxSlot) {
        int limit = this.getSizeLimit();
        Preconditions.checkArgument(maxSlot < limit, "maxSlot >= limit(" + limit + ")");
        Preconditions.checkArgument(minSlot >= 0, "minSlot < 0");

        for (int i = maxSlot; i < minSlot; i++) {
            if (this.isEmpty(i)) {
                return i;
            }
        }

        return -1;
    }

    protected List<Integer> findEmptySlots() {
        List<Integer> result = new ArrayList<>();

        int limit = this.getSizeLimit();

        for (int i = 0; i < limit; i++) {
            if (this.isEmpty(i)) {
                result.add(i);
            }
        }

        return result;
    }

    protected List<Integer> findEmptySlotsBetween(int minSlot, int maxSlot) {
        int limit = this.getSizeLimit();
        Preconditions.checkArgument(maxSlot < limit, "maxSlot >= limit(" + limit + ")");
        Preconditions.checkArgument(minSlot >= 0, "minSlot < 0");

        List<Integer> result = new ArrayList<>();

        for (int i = minSlot; i < maxSlot; i++) {
            if (this.isEmpty(i)) {
                result.add(i);
            }
        }

        return result;
    }

    protected boolean isEmpty(int slot) {
        return !this.buttonsMap.containsKey(slot);
    }

    protected boolean hasItem(int slot) {
        return !this.isEmpty(slot);
    }

    public Menu clear() {
        this.buttonsMap.clear();
        return this;
    }

    public void clear(int x, int y) {
        this.clear(getSlot(x, y));
    }

    public void clear(int slot) {
        this.buttonsMap.remove(slot);
    }

    public Menu clearBetween(int minSlot, int maxSlot) {
        this.buttonsMap.entrySet().removeIf(entry -> entry.getKey() >= minSlot && entry.getKey() <= maxSlot);
        return this;
    }

    public final void open(Player player) {
        if (this.opening) {
            throw new IllegalArgumentException("The menu is already opened!");
        }
        this.opening = true;
        this.openMillis = System.currentTimeMillis();

        this.player = player;
        Metadata.provide(player).put(METADATA, this);
        Menu.addMenu(this);

        this.render(true);
        this.onOpen(player);

        this.registerListeners();
    }

    public void close() {
        player.closeInventory();
    }

    public void remove() {
        if (!this.opening) {
            return;
        }
        if (this.reopening) {
            return;
        }
        this.opening = false;

        MetadataMap metadataMap = Metadata.provideForPlayer(this.player);
        Menu existing = metadataMap.getOrNull(METADATA);
        if (existing == this) {
            metadataMap.remove(METADATA);
        }
        Menu.removeMenu(this);

        this.compositeTerminable.closeAndReportException();
        this.onClose(player);

        MenuCloseEvent event = new MenuCloseEvent(player, this);
        event.call();

        this.player = null;
        this.buttonsMap.clear();
        this.inventory.clear();
    }

    private void registerListeners() {
        bukkitEventNode.addChild(this.eventNode);
    }

    public final void render() {
        this.render(false);
    }

    private void render(boolean firstInitial) {
        if (this.rendering) {
            return;
        }
        this.lastAccessMillis = System.currentTimeMillis();

        Map<Integer, Button> previousButtons = firstInitial ? null : ImmutableMap.copyOf(this.buttonsMap);

        this.rendering = true;
        this.draw(firstInitial);
        this.rendering = false;

        Inventory inventory = null;
        int size = this.getSize() == -1 ? this.size(this.buttonsMap) : this.getSize();

        String title = CC.translate(this.getTitle());
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        final InventoryView openInventory = player.getOpenInventory();
        String topInventory = openInventory.getTitle();
        if (this.inventory != null && title.equals(topInventory) && size == openInventory.getTopInventory().getSize()) {
            inventory = this.inventory;
        }

        boolean recreate = false;
        if (inventory == null) {
            inventory = Bukkit.createInventory(player, size, title);
            recreate = true;
        }

        for (final Map.Entry<Integer, Button> buttonEntry : this.buttonsMap.entrySet()) {
            int slot = buttonEntry.getKey();
            Button button = buttonEntry.getValue();
            if (previousButtons == null || previousButtons.get(slot) != button) {
                inventory.setItem(slot, button.getButtonItem(player));
            }
        }

        if (previousButtons != null) {
            for (int slot : previousButtons.keySet()) {
                if (!this.buttonsMap.containsKey(slot)) {
                    inventory.setItem(slot, null);
                }
            }
        }

        this.inventory = inventory;
        if (recreate) {
            this.rerendering = true;
            player.closeInventory();
            player.openInventory(inventory);
            this.rerendering = false;
        } else {
            player.updateInventory();
        }
    }

    public abstract void draw(boolean firstInitial);

    public int size(final Map<Integer, Button> buttons) {
        int highest = buttons.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(0);

        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    public static int getSlot(final int x, final int y) {
        return ((9 * y) + x);
    }

    public int getSize() {
        return -1;
    }

    public int getSizeLimit() {
        int size = this.getSize();
        if (size == -1) {
            size = 9 * 6;
        }
        return size;
    }

    public final <T> List<Button> transformToButtons(Iterable<T> list,
                                                     Function<T, @Nullable Button> function) {
        final ImmutableList.Builder<Button> listBuilder = new ImmutableList.Builder<>();

        for (T t : list) {
            Button button = function.apply(t);
            if (button == null)
                continue;
            listBuilder.add(button);
        }

        return listBuilder.build();
    }

    public final <T> List<Button> transformToButtonsWithIndex(Iterable<T> list,
                                                              BiFunction<T, Integer, @Nullable Button> function) {
        final ImmutableList.Builder<Button> listBuilder = new ImmutableList.Builder<>();

        int index = 0;
        for (T t : list) {
            Button button = function.apply(t, index);
            if (button == null)
                continue;
            listBuilder.add(button);
            index++;
        }

        return listBuilder.build();
    }

    public abstract String getTitle();

    public void onOpen(final Player player) {
    }

    public void onClose(final Player player) {
    }

    private static void addMenu(Menu menu) {
        List<Menu> list;
        final Class<? extends Menu> type = menu.getClass();
        if (MENU_BY_TYPE.containsKey(type)) {
            list = MENU_BY_TYPE.get(type);
        } else {
            list = new ArrayList<>();
            MENU_BY_TYPE.put(type, list);
        }

        list.add(menu);
    }

    private static void removeMenu(Menu menu) {
        final Class<? extends Menu> type = menu.getClass();
        if (!MENU_BY_TYPE.containsKey(type)) {
            return;
        }

        final List<Menu> list = MENU_BY_TYPE.get(type);
        list.remove(menu);

        if (list.isEmpty()) {
            MENU_BY_TYPE.remove(type);
        }
    }

    public static Menu getMenuByUuid(UUID uuid) {
        return Metadata.provideForPlayer(uuid).getOrNull(METADATA);
    }

    public static <T extends Menu> List<T> getMenusByType(Class<T> type) {
        List<T> menuList = new ArrayList<>();
        for (Menu menu : MENU_BY_TYPE.getOrDefault(type, Collections.emptyList())) {
            menuList.add(type.cast(menu));
        }
        return menuList;
    }

}