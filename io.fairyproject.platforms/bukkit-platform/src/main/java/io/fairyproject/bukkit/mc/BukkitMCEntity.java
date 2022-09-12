package io.fairyproject.bukkit.mc;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.event.EventNode;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCEventFilter;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.event.trait.MCEntityEvent;
import io.fairyproject.mc.util.Pos;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BukkitMCEntity implements MCEntity {

    private final Entity entity;
    private final EventNode<MCEntityEvent> eventNode;

    public BukkitMCEntity(Entity entity) {
        this.entity = entity;
        this.eventNode = GlobalEventNode.get().map(this, MCEventFilter.ENTITY);
    }

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
    public @NotNull EventNode<MCEntityEvent> eventNode() {
        return this.eventNode;
    }

    @Override
    public Pos pos() {
        return BukkitPos.toMCPos(this.entity.getLocation());
    }

    @Override
    public @NotNull List<EntityData> data() {
        return ThrowingSupplier.sneaky(() -> BukkitDataWatcherConverter.convert(this.entity)).get();
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.entity)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.entity);
    }
}
