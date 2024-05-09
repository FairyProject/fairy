package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEventsAPI;

public interface PacketEventsBuilder {

    PacketEventsAPI<?> build();

}
