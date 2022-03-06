package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.Pos;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;

import java.util.UUID;

@RequiredArgsConstructor
public class BukkitMCEntity implements MCEntity {

    private final Entity entity;

    @Override
    public MCWorld getWorld() {
        return MCWorld.from(this.entity.getWorld());
    }

    @Override
    public UUID getUUID() {
        return this.entity.getUniqueId();
    }

    @Override
    public int getId() {
        return this.entity.getEntityId();
    }

    @Override
    public boolean teleport(Pos pos) {
        return this.entity.teleport(BukkitPos.toBukkitLocation(pos));
    }

    @Override
    public Pos pos() {
        return BukkitPos.toMCPos(this.entity.getLocation());
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.entity)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.entity);
    }
}
