package io.fairyproject.bukkit.protocol.packet.artemispacketapi;

import io.fairyproject.bukkit.protocol.packet.PacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCPlayer;

public abstract class ArtemisPacketWrapper<T> extends PacketWrapper<T> {
    public ArtemisPacketWrapper(T wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }
}
