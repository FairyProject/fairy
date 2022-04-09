package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.Fairy;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.impl.BukkitPacketEventsBuilder;
import lombok.Getter;

@Getter
@Service
public class MCProtocol {
    public static MCVersion OVERWRITTEN_VERSION;
    public static MCProtocol INSTANCE;

    private PacketEventsAPI<?> packetEvents;

    private MCProtocol() {
        INSTANCE = this;
    }

    @PreInitialize
    public void onPreInitialize() {
        PacketEventsBuilder packetEventsBuilder;

        switch (Fairy.getPlatform().getPlatformType()) {
            case BUKKIT:
                packetEventsBuilder = new BukkitPacketEventsBuilder();
                break;
            default:
                throw new UnsupportedOperationException("The current platform aren't supported for protocol module.");
        }

        this.packetEvents = packetEventsBuilder.build();
        PacketEvents.setAPI(this.packetEvents);
        this.packetEvents.load();
    }

    @PostInitialize
    public void onPostInitialize() {
        this.packetEvents.getSettings()
                .debug(false)
                .bStats(false)
                .checkForUpdates(false);
        this.packetEvents.init();
    }

    @PostDestroy
    public void onPostDestroy() {
        this.packetEvents.terminate();
    }

    public MCVersion version() {
        if (OVERWRITTEN_VERSION != null) {
            return OVERWRITTEN_VERSION;
        }
        return MCVersion.getVersionFromRaw(
                PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion()
        );
    }

    public static void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper) {
        MCProtocol.INSTANCE.getPacketEvents()
                .getProtocolManager()
                .sendPacket(mcPlayer.getChannel(), packetWrapper);
    }

}
