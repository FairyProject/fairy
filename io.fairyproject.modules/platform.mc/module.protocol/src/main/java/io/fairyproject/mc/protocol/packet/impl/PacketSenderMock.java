package io.fairyproject.mc.protocol.packet.impl;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.PacketSender;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketSenderMock implements PacketSender {

    private static final PacketSenderMock INSTANCE = new PacketSenderMock();
    private final Map<UUID, PacketPool> pools = new ConcurrentHashMap<>();

    @Override
    public void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper) {
        this.getPool(mcPlayer).add(packetWrapper);
    }

    public PacketPool getPool(MCPlayer mcPlayer) {
        return this.pools.computeIfAbsent(mcPlayer.getUUID(), i -> new PacketPool(mcPlayer.getUUID()));
    }

    public static PacketSenderMock get() {
        return INSTANCE;
    }

}
