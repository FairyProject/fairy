package io.fairyproject.bukkit.util;

import io.fairyproject.mc.util.Position;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;

@UtilityClass
public class BukkitPos {

    public Location toBukkitLocation(Position pos) {
        return new Location(pos.getMCWorld().as(World.class),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getYaw(),
                pos.getPitch()
        );
    }

    public Position toMCPos(Location location) {
        return new Position(location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

}
