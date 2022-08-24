package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.metadata.MetadataMap;
import net.kyori.adventure.audience.Audience;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

public class BukkitMCWorld implements MCWorld {

    private final World world;

    public BukkitMCWorld(World world) {
        this.world = world;
    }

    @Override
    public String name() {
        return this.world.getName();
    }

    @Override
    public List<MCPlayer> players() {
        return this.world.getPlayers().stream()
                .map(MCPlayer::from)
                .collect(Collectors.toList());
    }

    @Override
    public MetadataMap metadata() {
        return Metadata.provideForWorld(this.world);
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

    @Override
    public Audience audience() {
        return FairyBukkitPlatform.AUDIENCES.all();
    }
}
