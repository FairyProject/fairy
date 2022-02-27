package io.fairyproject.bukkit.protocol.packet;

import io.fairyproject.mc.protocol.check.Check;
import io.fairyproject.mc.protocol.check.type.ByteBufCheck;
import io.fairyproject.mc.protocol.check.type.PacketCheck;
import io.fairyproject.mc.protocol.data.PlayerData;
import io.fairyproject.mc.protocol.packet.LowLevelPacketListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.packet.PacketListener;
import io.fairyproject.mc.protocol.packet.PacketProvider;
import io.fairyproject.mc.protocol.spigot.AntiCrash;
import io.fairyproject.mc.protocol.spigot.manager.AbstractManager;
import io.fairyproject.mc.protocol.wrapper.bytebuf.ArtemisByteBuf;
import lombok.Getter;

public class PacketManager extends AbstractManager {
    @Getter
    private PacketProvider provider;

    public PacketManager(AntiCrash parent) {
        super(parent);
    }

    @Override
    public void load() {
        provider = new PacketProviderFactory()
                .setPacketListener(new PacketListener() {
                    @Override
                    public boolean onPacket(PlayerData data, Packet packet) {
                        for (Check check : data.getPacketChecks()) {
                            final PacketCheck packetCheck = (PacketCheck) check;

                            if (packetCheck.handle(packet))
                                return true;
                        }

                        return false;
                    }
                })
                .setLowLevelPacketListener(new LowLevelPacketListener() {
                    @Override
                    public boolean handle(PlayerData data, ArtemisByteBuf byteBuf) {
                        for (Check check : data.getPacketChecks()) {
                            final ByteBufCheck byteBufCheck = (ByteBufCheck) check;

                            if (byteBufCheck.handle(byteBuf))
                                return true;
                        }

                        return false;
                    }
                })
                .build();

        provider.load();
    }

    @Override
    public void init() {
        provider.init();
    }

    @Override
    public void end() {
        provider.quit();
    }
}
