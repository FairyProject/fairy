package io.fairyproject.tests.mc.protocol;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.PacketEventsBuilder;
import io.fairyproject.mc.protocol.packet.PacketSender;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import io.fairyproject.mc.version.MCVersionMappingRegistry;

public class MockMCProtocol extends MCProtocol {

    private final PacketEventsBuilder packetEventsBuilder;

    public MockMCProtocol(ContainerContext context,
                          MCPlayerRegistry playerRegistry,
                          MCVersionMappingRegistry mappingRegistry,
                          PacketSender packetSender,
                          PacketEventsBuilder packetEventsBuilder) {
        super(context, playerRegistry, mappingRegistry, packetSender);

        this.packetEventsBuilder = packetEventsBuilder;
    }

    @Override
    public void onPreInitialize() {
        loadProtocol(packetEventsBuilder);

        super.onPreInitialize();
    }
}
