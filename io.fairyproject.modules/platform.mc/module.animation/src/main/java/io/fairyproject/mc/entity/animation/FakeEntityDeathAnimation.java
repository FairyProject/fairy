package io.fairyproject.mc.entity.animation;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Pos;
import org.jetbrains.annotations.NotNull;

public class FakeEntityDeathAnimation extends AbstractFakeEntityAnimation {
    public FakeEntityDeathAnimation(@NotNull MCEntity entity) {
        super(entity);
    }

    @Override
    public void start() {
        MCEntity entity = this.entity();
        Pos pos = entity.pos();

        if (entity instanceof MCPlayer) {
            WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(
                    MCEntity.Companion.BRIDGE.newEntityId(),
                    entity.getUUID(),
                    new Vector3d(
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ),
                    pos.getYaw(),
                    pos.getPitch());
        }
    }
}
