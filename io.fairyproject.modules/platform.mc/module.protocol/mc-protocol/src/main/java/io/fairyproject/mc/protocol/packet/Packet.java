package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.protocol.netty.Channel;

public interface Packet {
    /**
     * Fancy name without the "PacketPlay(in/out)" prefix and instead annotated with
     * something more friendly for UI use.
     * @return String object representing the friendly name
     */
    String getFancyName();

    /**
     * Returns the Channel of which the packet is provided from. This is most usually
     * going to be as the format of a Netty channel.
     * @return Channel object of the packet
     */
    Channel getChannel();
}
