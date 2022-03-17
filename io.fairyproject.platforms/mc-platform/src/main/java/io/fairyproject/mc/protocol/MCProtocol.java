package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import lombok.Getter;

@Getter
public class MCProtocol {
    public static MCVersion OVERWRITTEN_VERSION;
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
        if (OVERWRITTEN_VERSION != null) {
            return OVERWRITTEN_VERSION;
        }
        return MCVersion.getVersionFromRaw(
                PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion()
        );
    }

}
