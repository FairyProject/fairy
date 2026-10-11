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
    private MCPlayer entity;
    private FakeEntityDeathAnimation animation;

    @BeforeEach
    void setup() {
        previousApi = PacketEvents.getAPI();
        PacketEventsAPI<?> api = mock(PacketEventsAPI.class, RETURNS_DEEP_STUBS);
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
        assertModernSpawn(MCVersion.of(26, 3, 0));
    }

    @Test
    void minecraft1202UsesEntitySpawnAndSeparateMetadata() {
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
    }

    @Test
    void minecraft1201RetainsDedicatedPlayerSpawn() {
        List<PacketWrapper<?>> packets = animation.createSpawnPackets(42, MCVersion.of(20, 1));
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof WrapperPlayServerSpawnPlayer);
        WrapperPlayServerSpawnPlayer spawn = (WrapperPlayServerSpawnPlayer) packets.get(0);
        assertEquals(42, spawn.getEntityId());
        assertEquals(entity.getUUID(), spawn.getUUID());
        assertEquals(entity.data(), spawn.getEntityMetadata());
    }
}
