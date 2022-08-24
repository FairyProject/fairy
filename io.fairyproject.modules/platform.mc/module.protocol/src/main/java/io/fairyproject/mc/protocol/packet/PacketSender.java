package io.fairyproject.mc.protocol.packet;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.mc.MCPlayer;

public interface PacketSender {

    void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper);

}
