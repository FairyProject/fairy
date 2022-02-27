package io.fairyproject.bukkit.protocol.packet.packetevents.v2;

import io.fairyproject.bukkit.protocol.packet.PacketWrapper;
import io.fairyproject.mc.MCPlayer;

public abstract class PacketEventWrapper<T> extends PacketWrapper<T> {
    public PacketEventWrapper(T wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }
}
