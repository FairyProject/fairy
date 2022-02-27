package io.fairyproject.bukkit.protocol.packet.artemispacketapi;

import io.fairyproject.bukkit.protocol.packet.PacketWrapper;
import io.fairyproject.mc.protocol.netty.Channel;

public abstract class ArtemisPacketWrapper<T> extends PacketWrapper<T> {
    public ArtemisPacketWrapper(T wrapper, Channel channel) {
        super(wrapper, channel);
    }
}
