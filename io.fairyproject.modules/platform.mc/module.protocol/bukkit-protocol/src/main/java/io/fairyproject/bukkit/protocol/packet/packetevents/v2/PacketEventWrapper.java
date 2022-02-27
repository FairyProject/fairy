package io.fairyproject.bukkit.protocol.packet.packetevents.v2;

import io.fairyproject.bukkit.protocol.packet.PacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;

public abstract class PacketEventWrapper<T> extends PacketWrapper<T> {
    public PacketEventWrapper(T wrapper, Channel channel) {
        super(wrapper, channel);
    }
}
