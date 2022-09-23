package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.util.Position;
import io.fairyproject.tests.mc.MCPlayerMock;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitMCPlayerMock extends MCPlayerMock {

    private final Player player;

    public BukkitMCPlayerMock(UUID uuid, String name, MCVersion version, Player originalInstance) {
        super(uuid, name, version, originalInstance);
        this.player = originalInstance;
    }

    @Override
    public MCWorld getWorld() {
        return MCWorld.from(this.player.getWorld());
    }

    @Override
    public Position pos() {
        return BukkitPos.toMCPos(this.player.getLocation());
    }

    @Override
    public boolean teleport(Position pos) {
        return this.player.teleport(BukkitPos.toBukkitLocation(pos));
    }
}
