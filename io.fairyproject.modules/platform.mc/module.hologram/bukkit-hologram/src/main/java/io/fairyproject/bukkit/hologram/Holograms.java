package io.fairyproject.bukkit.hologram;

import io.fairyproject.bukkit.metadata.Metadata;
import lombok.experimental.UtilityClass;
import org.bukkit.World;

@UtilityClass
public class Holograms {

    public HologramHandler getHolograms(World world) {
        return Metadata.provideForWorld(world).getOrPut(HologramHandler.WORLD_METADATA, () -> new HologramHandler(world));
    }

}
