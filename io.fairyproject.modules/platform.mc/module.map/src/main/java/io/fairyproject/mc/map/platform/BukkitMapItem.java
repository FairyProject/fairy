package io.fairyproject.mc.map.platform;

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
        Material material;
        try {
            material = Material.FILLED_MAP;
        } catch (NoSuchFieldError ex) {
            material = Material.MAP;
        }
        return new ItemStack(material, 1, (short) MapService.MAP_ID);
    }

}
