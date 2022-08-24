package io.fairyproject.mc.protocol.impl;

import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.mc.protocol.PacketEventsBuilder;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

public class BukkitPacketEventsBuilder implements PacketEventsBuilder {
    @Override
    public PacketEventsAPI<?> build() {
        return SpigotPacketEventsBuilder.build(FairyBukkitPlatform.PLUGIN);
    }
}
