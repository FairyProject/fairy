package io.fairyproject.bukkit.protocol.packet.artemispacketapi;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.protocol.packet.artemispacketapi.translate.ArtemisPacketTranslationHelper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.injector.PacketEventsInjector;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.InternalBufferListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.InternalPacketListener;
import io.fairyproject.mc.protocol.PacketProvider;
import ac.artemis.packet.profile.Profile;
import ac.artemis.packet.spigot.wrappers.GPacket;
import cc.ghast.packet.PacketAPI;
import cc.ghast.packet.PacketManager;
import cc.ghast.packet.utils.Pair;

import java.util.UUID;

public class PacketArtemisProvider extends PacketProvider  {
    public PacketArtemisProvider(InternalPacketListener highListener, InternalBufferListener lowListener) {
        super(highListener, lowListener, new PacketEventsInjector());
    }

    @Override
    public void load() {
        // Do nothing
    }

    @Override
    public void init() {
        if (PacketManager.INSTANCE.getApi() == null) {
            PacketManager.INSTANCE.init(FairyBukkitPlatform.PLUGIN);
        }

        PacketAPI.addListener(new ac.artemis.packet.PacketListener() {
            @Override
            public void onPacket(Profile profile, ac.artemis.packet.wrapper.Packet wrapper) {
                final MCPlayer data = MCPlayer.find(profile.getUuid());

                if (data == null) {
                    return;
                }

                final Packet packet = ArtemisPacketTranslationHelper.PACKET.transform(new Pair<>(profile, (GPacket) wrapper));

                if (packet == null)
                    return;

                final UUID uuid = profile.getUuid();

                if (!injectQueue.isEmpty() && injectQueue.contains(uuid)) {
                    injector.inject(data, packet.getPlayer(), lowListener);
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
    public void inject(MCPlayer data) {
        injectQueue.add(data.getUUID());
    }
}
