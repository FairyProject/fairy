package io.fairyproject.mc.protocol.impl.mock;

import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.PacketEventsBuilder;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockPacketEventsBuilder implements PacketEventsBuilder {

    private final MCServer mcServer;
    private final MCVersionMappingRegistry mappingRegistry;

    @Override
    public PacketEventsAPI<?> build() {
        return new MockPacketEventsAPI(mcServer, mappingRegistry);
    }
}
