package io.fairyproject.bukkit.util;

import io.fairyproject.mc.util.Pos;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;

@UtilityClass
public class BukkitPos {

    public Location toBukkitLocation(Pos pos) {
        return new Location(pos.getMCWorld().as(World.class),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getYaw(),
                pos.getPitch()
        );
    }

    public Pos toMCPos(Location location) {
        return new Pos(location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

}
