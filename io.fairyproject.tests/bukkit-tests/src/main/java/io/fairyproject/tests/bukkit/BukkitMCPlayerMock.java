package io.fairyproject.tests.bukkit;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.Position;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.tests.mc.MCPlayerMock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BukkitMCPlayerMock extends MCPlayerMock {

    private final Player player;

    public BukkitMCPlayerMock(UUID uuid, String name, MCVersion version, Player originalInstance, MCVersionMappingRegistry versionMappingRegistry) {
        super(uuid, name, version, originalInstance, versionMappingRegistry);
        this.player = originalInstance;
    }

    @Override
    public MCWorld getWorld() {
        return MCWorld.from(this.player.getWorld());
    }

    @Override
    public Position getPosition() {
        return BukkitPos.toMCPos(this.player.getLocation());
    }

    @Override
    public boolean teleport(Position pos) {
        return this.player.teleport(BukkitPos.toBukkitLocation(pos));
    }

    @Override
    public @NotNull List<EntityData> data() {
        throw new UnsupportedOperationException();
    }
}
