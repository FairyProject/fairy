package io.fairyproject.bukkit.protocol.packet;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.Packet;

public abstract class PacketWrapper<T> implements Packet {
    protected T wrapper;
    protected MCPlayer player;

    public PacketWrapper(T wrapper, MCPlayer player) {
        this.wrapper = wrapper;
        this.player = player;
    }

    @Override
    public io.fairyproject.mc.MCPlayer getPlayer() {
        return player;
    }
}
