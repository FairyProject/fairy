package io.fairyproject.bukkit.hologram;

import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.module.Modular;
import lombok.experimental.UtilityClass;
import org.bukkit.World;

@Modular(
        value = "bukkit-hologram"
)
@UtilityClass
public class Holograms {

    public HologramHandler getHolograms(World world) {
        return Metadata.provideForWorld(world).getOrPut(HologramHandler.WORLD_METADATA, () -> new HologramHandler(world));
    }

}
