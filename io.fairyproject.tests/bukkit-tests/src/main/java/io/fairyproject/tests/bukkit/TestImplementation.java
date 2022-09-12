package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.impl.annotation.ServerImpl;
import io.fairyproject.bukkit.impl.server.ServerImplementation;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.player.movement.impl.AbstractMovementImplementation;
import io.fairyproject.bukkit.player.movement.impl.BukkitMovementImplementation;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

@ServerImpl(Integer.MAX_VALUE)
public class TestImplementation implements ServerImplementation {
    @Override
    public Entity getEntity(UUID uuid) {
        return MockBukkitContext.get().getServer().getEntity(uuid);
    }

    @Override
    public Entity getEntity(World world, int id) {
        return world.getEntities().stream()
                .filter(entity -> entity.getEntityId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new BukkitMovementImplementation(movementListener);
    }
}
