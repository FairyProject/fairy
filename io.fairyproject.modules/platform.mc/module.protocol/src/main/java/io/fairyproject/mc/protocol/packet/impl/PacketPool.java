package io.fairyproject.mc.protocol.packet.impl;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketPool {

    @Getter
    private final UUID uuid;
    private final Queue<PacketWrapper<?>> packets;

    public PacketPool(UUID uuid) {
        this.uuid = uuid;
        this.packets = new ConcurrentLinkedQueue<>();
    }

    public void add(PacketWrapper<?> packetWrapper) {
        this.packets.add(packetWrapper);
    }

    public PacketWrapper<?> peak() {
        return this.packets.peek();
    }

    public PacketWrapper<?> poll() {
        return this.packets.poll();
    }

}
