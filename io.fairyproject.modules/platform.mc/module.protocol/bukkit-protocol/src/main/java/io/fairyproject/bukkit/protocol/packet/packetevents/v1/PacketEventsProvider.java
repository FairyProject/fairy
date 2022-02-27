package io.fairyproject.bukkit.protocol.packet.packetevents.v1;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.injector.PacketEventsInjector;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.translate.PacketEventsTranslators;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.*;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.annotation.PacketHandler;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

import java.util.UUID;

public class PacketEventsProvider extends PacketProvider {
    public PacketEventsProvider(PacketListener highListener, BufferListener lowListener) {
        super(highListener, lowListener, new PacketEventsInjector());
    }

    @Override
    public void load() {
        PacketEvents.create(FairyBukkitPlatform.PLUGIN);
        PacketEvents.get().getSettings()
                .compatInjector(false)
                .checkForUpdates(false)
                .bStats(false)
                .fallbackServerVersion(ServerVersion.v_1_8_8);

        PacketEvents.get().loadAsyncNewThread();
    }

    @Override
    public void init() {
        PacketEvents.get().init();
        PacketEvents.get().getEventManager().registerListener(new io.github.retrooper.packetevents.event.PacketListener() {
            @PacketHandler
            private void handle(PacketPlayReceiveEvent packetPlayReceiveEvent) {
                final MCPlayer data = MCPlayer.find(packetPlayReceiveEvent.getPlayer().getUniqueId());

                if (data == null) {
                    return;
                }

                final Packet packet = PacketEventsTranslators.PACKET.transform(packetPlayReceiveEvent);

                if (packet == null)
                    return;

                final UUID uuid = packetPlayReceiveEvent.getPlayer().getUniqueId();

                if (!injectQueue.isEmpty() && injectQueue.contains(uuid)) {
                    injector.inject(data, packet.getChannel(), lowListener);
                    injectQueue.remove(uuid);
                }

                final boolean cancel = highListener.onPacket(data, packet);
                packetPlayReceiveEvent.setCancelled(cancel);
            }
        });
    }

    @Override
    public void quit() {
        PacketEvents.get().stop();
    }

    @Override
    public void inject(MCPlayer data) {
        injectQueue.add(data.getUUID());
    }
}
