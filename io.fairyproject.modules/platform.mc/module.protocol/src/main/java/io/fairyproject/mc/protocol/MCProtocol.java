package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.impl.BukkitPacketEventsBuilder;
import io.fairyproject.mc.protocol.impl.mock.MockPacketEventsBuilder;
import io.fairyproject.mc.protocol.packet.PacketSender;
import io.fairyproject.mc.protocol.packet.impl.PacketSenderImpl;
import io.fairyproject.mc.protocol.packet.impl.PacketSenderMock;
import io.fairyproject.util.terminable.Terminable;
import lombok.Getter;

@Getter
@Service
public class MCProtocol {
    public static MCProtocol INSTANCE;

    private PacketSender packetSender;
    private PacketEventsAPI<?> packetEvents;

    private MCProtocol() {
        INSTANCE = this;
    }

    @PreInitialize
    public void onPreInitialize() {
        PacketEventsBuilder packetEventsBuilder;
        PacketSender packetSender;

        if (Debug.UNIT_TEST) {
            packetEventsBuilder = new MockPacketEventsBuilder();
            packetSender = PacketSenderMock.get();
        } else {
            packetSender = new PacketSenderImpl();
            switch (Fairy.getPlatform().getPlatformType()) {
                case BUKKIT:
                    packetEventsBuilder = new BukkitPacketEventsBuilder();
                    break;
                default:
                    throw new UnsupportedOperationException("The current platform aren't supported for protocol module.");
            }
        }

        this.packetEvents = packetEventsBuilder.build();
        PacketEvents.setAPI(this.packetEvents);
        this.packetEvents.load();

        this.packetSender = packetSender;
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

    public static void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper) {
        MCProtocol.INSTANCE.getPacketSender().sendPacket(mcPlayer, packetWrapper);
    }

    public static Terminable listen(PacketListenerCommon packetListener) {
        MCProtocol.INSTANCE.getPacketEvents().getEventManager().registerListener(packetListener);
        return () -> MCProtocol.INSTANCE.getPacketEvents().getEventManager().unregisterListener(packetListener);
    }

}
