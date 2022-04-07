package io.fairyproject.mc.hologram;

import io.fairyproject.mc.MCWorld;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Holograms {

    @NotNull
    public HologramFactory factory(MCWorld world) {
        return world.metadata().getOrPut(HologramFactory.WORLD_METADATA, () -> new HologramFactory(world));
    }

    @Nullable
    public HologramFactory find(MCWorld world) {
        return world.metadata().getOrNull(HologramFactory.WORLD_METADATA);
    }

}
