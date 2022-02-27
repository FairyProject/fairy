package io.fairyproject.mc.protocol;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.Packet;

public interface InternalPacketListener {
    boolean onPacket(final MCPlayer data, final Packet packet);
}
