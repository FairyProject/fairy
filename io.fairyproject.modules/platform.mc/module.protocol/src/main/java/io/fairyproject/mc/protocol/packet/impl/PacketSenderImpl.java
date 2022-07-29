package io.fairyproject.mc.protocol.packet.impl;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.Debug;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.packet.PacketSender;

public class PacketSenderImpl implements PacketSender {
    @Override
    public void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper) {
        if (Debug.UNIT_TEST) {
            throw new IllegalStateException("PacketSenderImpl shouldn't be used in unit test runtime.");
        }
        MCProtocol.INSTANCE.getPacketEvents()
                .getProtocolManager()
                .sendPacket(mcPlayer.getChannel(), packetWrapper);
    }
}
