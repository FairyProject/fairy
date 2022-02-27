package io.fairyproject.bukkit.protocol.packet.packetevents.v1;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.spigot.packet.PacketWrapper;

public abstract class PacketEventWrapper<T> extends PacketWrapper<T> {
    public PacketEventWrapper(T wrapper, Channel channel) {
        super(wrapper, channel);
    }
}
