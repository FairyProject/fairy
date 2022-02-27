package io.fairyproject.bukkit.protocol.packet.artemispacketapi.netty;

import io.fairyproject.mc.protocol.netty.Channel;

public class ArtemisChannel implements Channel {
    private final io.netty.channel.Channel channel;

    public ArtemisChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
    }

    @Override
    public void close() {
        if (!channel.isOpen() || channel.pipeline() == null)
            return;

        channel.close();
    }

    public io.netty.channel.Channel getChannel() {
        return channel;
    }
}
