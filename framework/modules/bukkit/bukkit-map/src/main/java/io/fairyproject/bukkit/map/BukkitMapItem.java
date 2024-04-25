package io.fairyproject.bukkit.map;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.mc.map.MapService;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

@UtilityClass
public class BukkitMapItem {

    static {
        Events.subscribe(MapInitializeEvent.class)
                .listen(event -> {
                    final MapView map = event.getMap();
                    if (map.getId() == MapService.MAP_ID)
                        map.getRenderers().forEach(map::removeRenderer);
                })
                .build(FairyBukkitPlatform.PLUGIN);
    }

    public ItemStack create() {
        try {
            ItemStack item = new ItemStack(Material.FILLED_MAP, 1);

            org.bukkit.inventory.meta.MapMeta meta = (org.bukkit.inventory.meta.MapMeta) item.getItemMeta();
            meta.setMapId(MapService.MAP_ID);
            item.setItemMeta(meta);

            return item;
        } catch (Throwable ex) {
        }
        return new ItemStack(Material.MAP, 1, (short) MapService.MAP_ID);
    }

}
