package org.fairy.bukkit.player;

import org.bukkit.entity.Player;
import org.fairy.mc.MCPlayer;

import java.util.UUID;

public class BukkitMCPlayer implements MCPlayer {

    private final Player player;

    public BukkitMCPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.player)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.player);
    }
}
