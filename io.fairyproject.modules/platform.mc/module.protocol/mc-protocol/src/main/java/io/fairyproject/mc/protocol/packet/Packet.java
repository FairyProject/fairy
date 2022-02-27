package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.MCPlayer;

public interface Packet {
    /**
     * Fancy name without the "PacketPlay(in/out)" prefix and instead annotated with
     * something more friendly for UI use.
     * @return String object representing the friendly name
     */
    String getFancyName();

    /**
     * Returns the Player of which the packet is provided from. This is most usually
     * going to be as the format of a MCPlayer.
     * @return MCPlayer object of the packet
     */
    MCPlayer getPlayer();
}
