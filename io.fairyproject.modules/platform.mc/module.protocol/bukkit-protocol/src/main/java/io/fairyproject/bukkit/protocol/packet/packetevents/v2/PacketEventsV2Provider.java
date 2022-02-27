package io.fairyproject.bukkit.protocol.packet.packetevents.v2;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.injector.PacketEventsInjector;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.translate.PacketEventsTranslationHelper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.InternalBufferListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.InternalPacketListener;
import io.fairyproject.mc.protocol.PacketProvider;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

import java.util.UUID;

public class PacketEventsV2Provider extends PacketProvider {
    public PacketEventsV2Provider(InternalPacketListener highListener, InternalBufferListener lowListener) {
        super(highListener, lowListener, new PacketEventsInjector());
    }

    @Override
    public void load() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(FairyBukkitPlatform.PLUGIN));
        PacketEvents.getAPI().load();
    }

    @Override
    public void init() {
        PacketEvents.getAPI().getSettings()
                .debug(false)
                .bStats(false)
                .checkForUpdates(true);
        PacketEvents.getAPI().init();

        SimplePacketListenerAbstract listener = new SimplePacketListenerAbstract(PacketListenerPriority.HIGH,
                true, false) {
            @Override
            public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
                final UUID uuid = event.getUser().getProfile().getUUID();
                final MCPlayer data = MCPlayer.find(uuid);

                if (data == null) {
                    return;
                }

                final Packet packet = PacketEventsTranslationHelper.PACKET.transform(event);

                if (packet == null)
                    return;


                if (!injectQueue.isEmpty() && injectQueue.contains(uuid)) {
                    injector.inject(data, packet.getPlayer(), lowListener);
                    injectQueue.remove(uuid);
                }

                final boolean cancel = highListener.onPacket(data, packet);
                event.setCancelled(cancel);
            }
        };

        PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    @Override
    public void quit() {
        PacketEvents.getAPI().terminate();
    }

    @Override
    public void inject(MCPlayer data) {
        injectQueue.add(data.getUUID());
    }
}
