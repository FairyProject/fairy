package io.fairyproject.bukkit.protocol.packet.artemispacketapi;

import io.fairyproject.mc.protocol.data.PlayerData;
import io.fairyproject.mc.protocol.packet.LowLevelPacketListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.packet.PacketListener;
import io.fairyproject.mc.protocol.packet.PacketProvider;
import io.fairyproject.mc.protocol.spigot.Access;
import io.fairyproject.mc.protocol.spigot.packet.artemispacketapi.translate.ArtemisPacketTranslators;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.injector.PacketEventsInjector;
import ac.artemis.packet.profile.Profile;
import ac.artemis.packet.spigot.wrappers.GPacket;
import cc.ghast.packet.PacketAPI;
import cc.ghast.packet.PacketManager;
import cc.ghast.packet.utils.Pair;

import java.util.UUID;

public class PacketArtemisProvider extends PacketProvider implements Access {
    public PacketArtemisProvider(PacketListener highListener, LowLevelPacketListener lowListener) {
        super(highListener, lowListener, new PacketEventsInjector());
    }

    @Override
    public void load() {

    }

    @Override
    public void init() {
        if (PacketManager.INSTANCE.getApi() == null) {
            PacketManager.INSTANCE.init(plugin());
        }

        PacketAPI.addListener(new ac.artemis.packet.PacketListener() {
            @Override
            public void onPacket(Profile profile, ac.artemis.packet.wrapper.Packet wrapper) {
                final PlayerData data = playerDataManager().get(profile.getUuid());

                if (data == null) {
                    return;
                }

                final Packet packet = ArtemisPacketTranslators.PACKET.transform(new Pair<>(profile, (GPacket) wrapper));

                if (packet == null)
                    return;

                final UUID uuid = profile.getUuid();

                if (!injectQueue.isEmpty() && injectQueue.contains(uuid)) {
                    injector.inject(data, packet.getChannel(), lowListener);
                    injectQueue.remove(uuid);
                }

                final boolean cancel = highListener.onPacket(data, packet);
                ((GPacket) wrapper).setCancelled(cancel);
            }
        });
    }

    @Override
    public void quit() {
        PacketManager.INSTANCE.destroy();
    }

    @Override
    public void inject(PlayerData data) {
        injectQueue.add(data.getUuid());
    }
}
