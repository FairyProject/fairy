package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;

public interface PacketDeserializer {

    PacketDeserializer DEFAULT = (protocol, byteBuf, packetId, defaultType) -> {
        try {
            return defaultType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    };

    MCPacket deserialize(MCProtocolMapping.Protocol protocol, FriendlyByteBuf byteBuf, int packetId, Class<? extends MCPacket> defaultType);

}
