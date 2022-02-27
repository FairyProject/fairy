package io.fairyproject.bukkit.protocol.packet;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.Packet;

public abstract class PacketWrapper<T> implements Packet {
    protected T wrapper;
    protected Channel channel;

    public PacketWrapper(T wrapper, Channel channel) {
        this.wrapper = wrapper;
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
