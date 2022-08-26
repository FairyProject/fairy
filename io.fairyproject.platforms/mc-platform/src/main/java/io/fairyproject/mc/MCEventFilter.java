package io.fairyproject.mc;

import io.fairyproject.event.EventFilter;
import io.fairyproject.mc.event.MCPlayerMoveEvent;
import io.fairyproject.mc.event.trait.MCEntityEvent;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import io.fairyproject.mc.util.Pos;
import io.fairyproject.mc.util.math.CoordinateUtil;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class MCEventFilter {

    public static final EventFilter<MCEntityEvent, MCEntity> ENTITY = EventFilter.from(MCEntityEvent.class, MCEntity.class, MCEntityEvent::entity);
    public static final EventFilter<MCPlayerEvent, MCPlayer> PLAYER = EventFilter.from(MCPlayerEvent.class, MCPlayer.class, MCPlayerEvent::player);

    public static final Predicate<MCPlayerMoveEvent> DIFFERENT_CHUNK = event -> {
            Pos from = event.fromPos();
            Pos to = event.toPos();

            int oldChunkX = CoordinateUtil.worldToChunk(from.getBlockX());
            int oldChunkZ = CoordinateUtil.worldToChunk(from.getBlockZ());
            int newChunkX = CoordinateUtil.worldToChunk(to.getBlockX());
            int newChunkZ = CoordinateUtil.worldToChunk(to.getBlockZ());

            return oldChunkX != newChunkX || oldChunkZ != newChunkZ;
    };

}
