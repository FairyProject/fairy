package io.fairyproject.mc.protocol.packet;


import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.netty.Channel;

public interface PacketInjector {
    void inject(final MCPlayer data, final Channel channel, final BufferListener packetListener);
}
