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
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import io.fairyproject.mc.util.Position;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BukkitMCEntity implements MCEntity {

    private final BukkitDataWatcherConverter dataWatcherConverter;
    private final EventNode<MCEntityEvent> eventNode;

    private Entity entity;

    public BukkitMCEntity(BukkitDataWatcherConverter dataWatcherConverter) {
        this.dataWatcherConverter = dataWatcherConverter;
        this.eventNode = GlobalEventNode.get().map(this, MCEventFilter.ENTITY);
    }

    public BukkitMCEntity(Entity entity, BukkitDataWatcherConverter dataWatcherConverter) {
        this(dataWatcherConverter);
        this.entity = entity;
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
    public boolean teleport(Position pos) {
        return this.entity.teleport(BukkitPos.toBukkitLocation(pos));
    }

    @Override
    public @NotNull EventNode<MCEntityEvent> getEventNode() {
        return this.eventNode;
    }

    @Override
    public Position getPosition() {
        return BukkitPos.toMCPos(this.entity.getLocation());
    }

    @Override
    public @NotNull List<EntityData> data() {
        return ThrowingSupplier.sneaky(() -> this.dataWatcherConverter.convert(this.entity)).get();
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.entity)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.entity);
    }

    @Override
    public void setNative(@NotNull Object nativeObject) {
        this.entity = (Entity) nativeObject;
    }
}
