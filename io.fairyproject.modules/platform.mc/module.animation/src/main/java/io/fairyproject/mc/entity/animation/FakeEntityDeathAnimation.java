package io.fairyproject.mc.entity.animation;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.util.Position;
import io.fairyproject.task.Task;
import org.jetbrains.annotations.NotNull;

public class FakeEntityDeathAnimation extends AbstractFakeEntityAnimation {
    public FakeEntityDeathAnimation(@NotNull MCEntity entity) {
        super(entity);
    }

    @Override
    public void start() {
        MCEntity entity = this.getEntity();
        Position pos = entity.getPosition();

        int entityID = MCEntity.Companion.BRIDGE.newEntityId();
        if (entity instanceof MCPlayer) {
            WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(
                    entityID,
                    entity.getUUID(),
                    new Vector3d(
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ),
                    pos.getYaw(),
                    pos.getPitch(),
                    entity.data());
            this.getViewers().forEach(mcPlayer -> MCProtocol.sendPacket(mcPlayer, packet));
        }

        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus(entityID, 3);
        this.getViewers().forEach(mcPlayer -> MCProtocol.sendPacket(mcPlayer, packet));

        Task.runMainLater(() -> {
            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityID);
            this.getViewers().forEach(mcPlayer -> MCProtocol.sendPacket(mcPlayer, destroyPacket));
        }, 20L);
    }
}
