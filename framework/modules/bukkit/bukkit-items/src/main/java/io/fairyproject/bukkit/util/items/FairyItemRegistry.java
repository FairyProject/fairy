package io.fairyproject.bukkit.util.items;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.util.ConditionUtils;
import org.bukkit.Material;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@InjectableComponent
public class FairyItemRegistry {

    private final NBTKey itemNbtKey = NBTKey.create("fairy", "item", "name");
    private final Map<String, FairyItem> itemByName = new ConcurrentHashMap<>();
    private final Map<Plugin, List<FairyItem>> itemsByPlugin = new ConcurrentHashMap<>();
    private final NBTModifier nbtModifier;

    public FairyItemRegistry(NBTModifier nbtModifier) {
        this.nbtModifier = nbtModifier;
    }

    @PostInitialize
    public void onPostInitialize() {
        Events.subscribe(PluginDisableEvent.class)
                .filter(event -> itemsByPlugin.containsKey(event.getPlugin()))
                .listen(event -> {
                    final List<FairyItem> items = itemsByPlugin.remove(event.getPlugin());
                    items.forEach(item -> itemByName.remove(item.getName()));
                })
                .build(FairyBukkitPlatform.PLUGIN);
    }

    public boolean has(String name) {
        return this.itemByName.containsKey(name);
    }

    public void register(FairyItem item) {
        ConditionUtils.not(this.itemByName.containsKey(item.getName()), "Item with id " + item.getName() + " already exists");
        ConditionUtils.is(item.isClosed(), "Item is already registered");

        itemByName.put(item.getName(), item);

        item.init();
    }

    public void unregister(FairyItem item) {
        ConditionUtils.is(this.itemByName.containsKey(item.getName()), "Item with id " + item.getName() + " does not exist");
        ConditionUtils.not(item.isClosed(), "Item is already closed");

        this.itemByName.remove(item.getName());

        item.closeAndReportException();
    }

    public FairyItem get(String id) {
        return itemByName.get(id);
    }

    public FairyItem get(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        String key = this.nbtModifier.getString(itemStack, itemNbtKey);
        return key == null ? null : this.get(key);
    }

    public ItemStack set(@NotNull ItemStack itemStack, @NotNull FairyItem fairyItem) {
        return this.nbtModifier.setTag(itemStack, this.itemNbtKey, fairyItem.getName());
    }

}
