package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.mc.MCAdventure;
import lombok.Getter;

@Getter
public class MCProtocol {
    public static MCProtocol INSTANCE;

    private final PacketEventsAPI<?> packetEvents;

    public static void initialize(PacketEventsAPI<?> packetEvents) {
        new MCProtocol(packetEvents);

    }

    private MCProtocol(PacketEventsAPI<?> packetEvents) {
        INSTANCE = this;

        this.packetEvents = packetEvents;
    }

    public MCVersion version() {
        return MCVersion.getVersionFromRaw(
                PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion()
        );
    }

}
