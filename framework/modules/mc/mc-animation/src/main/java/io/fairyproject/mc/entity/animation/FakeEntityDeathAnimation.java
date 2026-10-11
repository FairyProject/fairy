package io.fairyproject.mc.entity.animation;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.entity.EntityIDCounter;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.util.Position;
import io.fairyproject.mc.version.MCVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FakeEntityDeathAnimation extends AbstractFakeEntityAnimation {
    public FakeEntityDeathAnimation(@NotNull MCEntity entity) {
        super(entity);
    }

    @Override
    public void start() {
        MCEntity entity = this.getEntity();
        int entityID = EntityIDCounter.current().next();
        if (entity instanceof MCPlayer) {
            MCVersion serverVersion = MCServer.current().getVersion();
            this.getViewers().forEach(mcPlayer -> this.createSpawnPackets(entityID, serverVersion)
                    .forEach(packet -> MCProtocol.sendPacket(mcPlayer, packet)));
        }

        this.getViewers().forEach(mcPlayer -> {
            MCProtocol.sendPacket(mcPlayer, new WrapperPlayServerEntityStatus(entityID, 3));

            mcPlayer.getScheduler().schedule(() -> MCProtocol.sendPacket(mcPlayer, new WrapperPlayServerDestroyEntities(entityID)), 20L);
        });
    }

    List<PacketWrapper<?>> createSpawnPackets(int entityID, MCVersion serverVersion) {
        MCEntity entity = this.getEntity();
        Position pos = entity.getPosition();
        Vector3d position = new Vector3d(pos.getX(), pos.getY(), pos.getZ());

        // The dedicated player spawn packet was removed in Minecraft 1.20.2.
        if (serverVersion.isHigherOrEqual(MCVersion.of(20, 2))) {
            return Arrays.asList(
                    new WrapperPlayServerSpawnEntity(entityID, Optional.of(entity.getUUID()), EntityTypes.PLAYER,
                            position, pos.getPitch(), pos.getYaw(), pos.getYaw(), 0, Optional.of(new Vector3d())),
                    new WrapperPlayServerEntityMetadata(entityID, entity.data())
            );
        }

        return Collections.singletonList(new WrapperPlayServerSpawnPlayer(
                entityID, entity.getUUID(), position, pos.getYaw(), pos.getPitch(), entity.data()));
    }

}
