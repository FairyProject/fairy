package io.fairyproject.bukkit.mc;

import io.fairyproject.mc.MCWorld;
import org.bukkit.World;

public class BukkitMCWorld implements MCWorld {

    private final World world;

    public BukkitMCWorld(World world) {
        this.world = world;
    }

    @Override
    public <T> T as(Class<T> worldClass) {
        if (!worldClass.isInstance(this.world)) {
            throw new ClassCastException();
        }
        return worldClass.cast(this.world);
    }

    @Override
    public int getMaxY() {
        return this.world.getMaxHeight();
    }

    @Override
    public int getMaxSectionY() {
        return (this.getMaxY() - 1) >> 4;
    }
}
