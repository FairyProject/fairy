package io.fairyproject.bukkit.protocol;

import io.fairyproject.bukkit.protocol.packet.PacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.Packet;

public interface PacketMap<W, I> {
    <T extends PacketWrapper<K>, K extends W> Packet wrap(MCPlayer player, I id, W obj);

    void inject();
}
