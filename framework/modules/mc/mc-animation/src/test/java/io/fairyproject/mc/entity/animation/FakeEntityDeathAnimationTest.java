package io.fairyproject.mc.entity.animation;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Position;
import io.fairyproject.mc.version.MCVersion;
import io.github.retrooper.packetevents.impl.netty.NettyManagerImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FakeEntityDeathAnimationTest {
    private PacketEventsAPI<?> previousApi;
    private PacketEventsAPI<?> api;
    private MCPlayer entity;
    private FakeEntityDeathAnimation animation;

    @BeforeEach
    void setup() {
        previousApi = PacketEvents.getAPI();
        api = mock(PacketEventsAPI.class, RETURNS_DEEP_STUBS);
        when(api.getServerManager().getVersion()).thenReturn(ServerVersion.V_26_3);
        when(api.getSettings()).thenReturn(new PacketEventsSettings());
        when(api.getNettyManager()).thenReturn(new NettyManagerImpl());
        PacketEvents.setAPI(api);
        entity = mock(MCPlayer.class);
        when(entity.getUUID()).thenReturn(UUID.randomUUID());
        when(entity.getPosition()).thenReturn(new Position(1, 2, 3, 45, 30));
        List<EntityData<?>> metadata = Collections.singletonList(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20));
        when(entity.data()).thenReturn(metadata);
        animation = new FakeEntityDeathAnimation(entity);
    }

    @AfterEach
    void cleanup() {
        PacketEvents.setAPI(previousApi);
    }

    @Test
    void minecraft263UsesEntitySpawnAndSeparateMetadata() {
        when(api.getServerManager().getVersion()).thenReturn(ServerVersion.V_26_3);
        assertModernSpawn(MCVersion.of(26, 3, 0));
    }

    @Test
    void minecraft1202UsesEntitySpawnAndSeparateMetadata() {
        when(api.getServerManager().getVersion()).thenReturn(ServerVersion.V_1_20_2);
        assertModernSpawn(MCVersion.of(20, 2));
    }

    private void assertModernSpawn(MCVersion version) {
        List<PacketWrapper<?>> packets = animation.createSpawnPackets(42, version);
        assertEquals(2, packets.size());
        WrapperPlayServerSpawnEntity spawn = (WrapperPlayServerSpawnEntity) packets.get(0);
        assertEquals(42, spawn.getEntityId());
        assertEquals(entity.getUUID(), spawn.getUUID().get());
        assertEquals(EntityTypes.PLAYER, spawn.getEntityType());
        assertEquals(45, spawn.getYaw());
        assertEquals(30, spawn.getPitch());
        WrapperPlayServerEntityMetadata metadata = (WrapperPlayServerEntityMetadata) packets.get(1);
        assertEquals(42, metadata.getEntityId());
        assertEquals(entity.data(), metadata.getEntityMetadata());
        packets.forEach(this::assertSerializable);
    }

    @Test
    void minecraft1201RetainsDedicatedPlayerSpawn() {
        when(api.getServerManager().getVersion()).thenReturn(ServerVersion.V_1_20_1);
        List<PacketWrapper<?>> packets = animation.createSpawnPackets(42, MCVersion.of(20, 1));
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof WrapperPlayServerSpawnPlayer);
        WrapperPlayServerSpawnPlayer spawn = (WrapperPlayServerSpawnPlayer) packets.get(0);
        assertEquals(42, spawn.getEntityId());
        assertEquals(entity.getUUID(), spawn.getUUID());
        assertEquals(entity.data(), spawn.getEntityMetadata());
        assertSerializable(spawn);
    }

    @Test
    void olderServerProtocolsRetainSerializablePlayerSpawn() {
        ServerVersion[] versions = {ServerVersion.V_1_8_8, ServerVersion.V_1_12_2,
                ServerVersion.V_1_16_5, ServerVersion.V_1_19_4};
        MCVersion[] mcVersions = {MCVersion.of(8, 8), MCVersion.of(12, 2),
                MCVersion.of(16, 5), MCVersion.of(19, 4)};
        for (int i = 0; i < versions.length; i++) {
            when(api.getServerManager().getVersion()).thenReturn(versions[i]);
            List<PacketWrapper<?>> packets = animation.createSpawnPackets(42, mcVersions[i]);
            assertEquals(1, packets.size());
            assertTrue(packets.get(0) instanceof WrapperPlayServerSpawnPlayer);
            assertSerializable(packets.get(0));
        }
    }

    @Test
    void otherModernServerProtocolsUseSerializableEntitySpawn() {
        when(api.getServerManager().getVersion()).thenReturn(ServerVersion.V_1_21_3);
        assertModernSpawn(MCVersion.of(21, 3));
        when(api.getServerManager().getVersion()).thenReturn(ServerVersion.V_26_2);
        assertModernSpawn(MCVersion.of(26, 2, 0));
    }

    private void assertSerializable(PacketWrapper<?> packet) {
        assertTrue(packet.getPacketTypeData().getPacketType().getId(
                api.getServerManager().getVersion().toClientVersion()) >= 0);
        ByteBuf buffer = Unpooled.buffer();
        try {
            packet.setBuffer(buffer);
            packet.write();
            assertTrue(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }
}
