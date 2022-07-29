package io.fairytest.mc.protocol.packet;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.retrooper.packetevents.protocol.world.Difficulty;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDifficulty;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.packet.impl.PacketPool;
import io.fairyproject.mc.protocol.packet.impl.PacketSenderMock;
import io.fairyproject.tests.bukkit.base.BukkitJUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PacketSenderTest extends BukkitJUnitJupiterBase {

    @Test
    public void defaultSenderShouldBeMockInUnitTest() {
        Assertions.assertEquals(PacketSenderMock.class, MCProtocol.INSTANCE.getPacketSender().getClass());
    }

    @Test
    public void senderGetPoolShouldReturnPlayersPool() {
        final MCPlayer mcPlayer = this.createMCPlayer();
        final PacketPool pool = PacketSenderMock.get().getPool(mcPlayer);

        Assertions.assertEquals(mcPlayer.getUUID(), pool.getUuid());
    }

    @Test
    public void sendPacketPollShouldReturn() {
        MCPlayer mcPlayer = this.createMCPlayer();
        PacketWrapper<?> packet = this.createPacket();

        MCProtocol.sendPacket(mcPlayer, packet);
        final PacketPool pool = PacketSenderMock.get().getPool(mcPlayer);

        Assertions.assertEquals(packet, pool.poll());
        Assertions.assertNull(pool.poll());
    }

    @Test
    public void poolPeakShouldNotDeleteElement() {
        MCPlayer mcPlayer = this.createMCPlayer();
        PacketWrapper<?> packet = this.createPacket();

        MCProtocol.sendPacket(mcPlayer, packet);
        PacketPool pool = PacketSenderMock.get().getPool(mcPlayer);

        Assertions.assertEquals(packet, pool.peak());
        Assertions.assertEquals(packet, pool.peak());
    }

    private MCPlayer createMCPlayer() {
        PlayerMock playerMock = this.server.addPlayer();
        return MCPlayer.from(playerMock);
    }

    private PacketWrapper<?> createPacket() {
        return new WrapperPlayServerDifficulty(Difficulty.EASY, true);
    }

}
