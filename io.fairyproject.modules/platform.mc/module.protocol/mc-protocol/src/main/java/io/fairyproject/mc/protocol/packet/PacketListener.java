package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.MCPlayer;

public interface PacketListener {
    boolean onPacket(final MCPlayer data, final Packet packet);
}
