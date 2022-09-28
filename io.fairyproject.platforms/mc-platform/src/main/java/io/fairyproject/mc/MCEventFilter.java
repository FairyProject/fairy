package io.fairyproject.mc;

import io.fairyproject.event.EventFilter;
import io.fairyproject.mc.event.MCPlayerMoveEvent;
import io.fairyproject.mc.event.trait.MCEntityEvent;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import io.fairyproject.mc.event.trait.MCWorldEvent;
import io.fairyproject.mc.util.Position;
import io.fairyproject.mc.util.math.CoordinateUtil;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class MCEventFilter {

    public static final EventFilter<MCEntityEvent, MCEntity> ENTITY = EventFilter.from(MCEntityEvent.class, MCEntity.class, MCEntityEvent::getEntity);
    public static final EventFilter<MCPlayerEvent, MCPlayer> PLAYER = EventFilter.from(MCPlayerEvent.class, MCPlayer.class, MCPlayerEvent::getPlayer);

    public static final EventFilter<MCWorldEvent, MCWorld> WORLD = EventFilter.from(MCWorldEvent.class, MCWorld.class, MCWorldEvent::getWorld);

    public static final Predicate<MCPlayerMoveEvent> DIFFERENT_CHUNK = event -> {
            Position from = event.getFromPos();
            Position to = event.getToPos();

            int oldChunkX = CoordinateUtil.worldToChunk(from.getBlockX());
            int oldChunkZ = CoordinateUtil.worldToChunk(from.getBlockZ());
            int newChunkX = CoordinateUtil.worldToChunk(to.getBlockX());
            int newChunkZ = CoordinateUtil.worldToChunk(to.getBlockZ());

            return oldChunkX != newChunkX || oldChunkZ != newChunkZ;
    };

}
