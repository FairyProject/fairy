package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.mc.MCAdventure;
import lombok.Getter;
import io.fairyproject.mc.protocol.netty.NettyInjector;

@Getter
public class MCProtocol {

    public static MCProtocol INSTANCE;
    public static void initialize(NettyInjector injector, PacketEventsAPI<?> packetEvents) {
        new MCProtocol(injector, packetEvents);

        MCAdventure.initialize();
    }

    private final NettyInjector injector;
    private final PacketEventsAPI<?> packetEvents;

    private MCProtocol(NettyInjector injector, PacketEventsAPI<?> packetEvents) {
        INSTANCE = this;

        this.injector = injector;
        this.packetEvents = packetEvents;
    }

    public MCVersion version() {
        return MCVersion.getVersionFromRaw(
                PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion()
        );
    }

}
