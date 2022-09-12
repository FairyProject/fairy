package io.fairyproject.tests.bukkit;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.util.Pos;
import io.fairyproject.tests.mc.MCPlayerMock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
    public Pos pos() {
        return BukkitPos.toMCPos(this.player.getLocation());
    }

    @Override
    public boolean teleport(Pos pos) {
        return this.player.teleport(BukkitPos.toBukkitLocation(pos));
    }

    @Override
    public @NotNull List<EntityData> data() {
        throw new UnsupportedOperationException();
    }
}
