package io.fairyproject.mc.protocol.impl.mock;

import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.mc.protocol.PacketEventsBuilder;

public class MockPacketEventsBuilder implements PacketEventsBuilder {
    @Override
    public PacketEventsAPI<?> build() {
        return new MockPacketEventsAPI();
    }
}
