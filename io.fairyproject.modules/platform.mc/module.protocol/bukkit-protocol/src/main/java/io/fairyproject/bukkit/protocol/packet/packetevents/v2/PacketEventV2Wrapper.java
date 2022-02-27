package io.fairyproject.bukkit.protocol.packet.packetevents.v2;

import io.fairyproject.bukkit.protocol.packet.PacketWrapper;
import io.fairyproject.mc.MCPlayer;

public abstract class PacketEventV2Wrapper<T> extends PacketWrapper<T> {
    public PacketEventV2Wrapper(T wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }
}
