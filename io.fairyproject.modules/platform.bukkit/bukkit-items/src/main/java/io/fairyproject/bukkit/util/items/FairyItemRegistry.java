package io.fairyproject.bukkit.util.items;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.util.ConditionUtils;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FairyItemRegistry {

    private final Map<String, FairyItem> itemByName = new ConcurrentHashMap<>();
    private final Map<Plugin, List<FairyItem>> itemsByPlugin = new ConcurrentHashMap<>();

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
        itemsByPlugin.computeIfAbsent(item.getPlugin(), plugin -> new ArrayList<>()).add(item);

        item.init();
    }

    public void unregister(FairyItem item) {
        ConditionUtils.is(this.itemByName.containsKey(item.getName()), "Item with id " + item.getName() + " does not exist");
        ConditionUtils.not(item.isClosed(), "Item is already closed");

        this.itemByName.remove(item.getName());

        List<FairyItem> items = this.itemsByPlugin.get(item.getPlugin());
        if (items != null)
            items.remove(item);

        item.closeAndReportException();
    }

    public FairyItem get(String id) {
        return itemByName.get(id);
    }

    public FairyItem get(ItemStack itemStack) {
        return FairyItemRef.get(itemStack);
    }

}
