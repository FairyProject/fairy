package io.fairyproject.mc.entity.animation;

import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class AbstractFakeEntityAnimation implements FakeEntityAnimation {

    private final Set<MCPlayer> viewers = ConcurrentHashMap.newKeySet();
    private final MCEntity entity;

    public AbstractFakeEntityAnimation(@NotNull MCEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean addViewer(@NotNull MCPlayer player) {
        return this.viewers.add(player);
    }

    @Override
    public boolean removeViewer(@NotNull MCPlayer player) {
        return this.viewers.remove(player);
    }

    @Override
    public @NotNull Set<@NotNull MCPlayer> getViewers() {
        return Collections.unmodifiableSet(this.viewers);
    }

    @Override
    public void addNearbyViewers(int viewDistance) {
        this.nearby(viewDistance).forEach(this::addViewer);
    }

    private Stream<MCPlayer> nearby(int viewDistance) {
        return this.entity.getWorld()
                .getPlayers().stream()
                .filter(player -> this.chunkDistanceTo(player.getPosition()) <= viewDistance);
    }

    private double chunkDistanceTo(Position target) {
        int hologramChunkX = this.entity.getPosition().getChunkX();
        int hologramChunkZ = this.entity.getPosition().getChunkZ();

        int targetChunkX = target.getChunkX();
        int targetChunkZ = target.getChunkZ();

        return Math.sqrt(Math.pow(hologramChunkX - targetChunkX, 2) + Math.pow(hologramChunkZ - targetChunkZ, 2));
    }

    @Override
    public @NotNull MCEntity getEntity() {
        return this.entity;
    }
}
