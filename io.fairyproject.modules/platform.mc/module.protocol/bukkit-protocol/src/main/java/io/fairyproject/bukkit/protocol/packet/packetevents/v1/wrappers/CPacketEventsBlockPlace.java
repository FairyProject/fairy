package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.translate.PacketEventsTranslators;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketBlockPlace;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class CPacketEventsBlockPlace extends PacketEventWrapper<WrappedPacketInBlockPlace> implements CPacketBlockPlace {
    public CPacketEventsBlockPlace(WrappedPacketInBlockPlace wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public Hand getHand() {
        final io.github.retrooper.packetevents.utils.player.Hand bridge = wrapper.getHand();
        return PacketEventsTranslators.HAND.transform(bridge);
    }

    @Override
    public Direction getDirection() {
        final io.github.retrooper.packetevents.utils.player.Direction bridge = wrapper.getDirection();
        return PacketEventsTranslators.DIRECTION.transform(bridge);
    }

    @Override
    public Vec3i getClickedBlock() {
        final Vector3i bridge = wrapper.getBlockPosition();
        return PacketEventsTranslators.VECTOR_3I.transform(bridge);
    }

    @Override
    public Optional<Vec3f> getClickedOffset() {
        final Optional<Vector3f> optionalBridge = wrapper.getCursorPosition();
        if (!optionalBridge.isPresent())
            return Optional.empty();

        final Vector3f bridge = optionalBridge.get();
        final Vec3f translated = PacketEventsTranslators.VECTOR_3F.transform(bridge);

        return Optional.of(translated);
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        return wrapper.getItemStack();
    }
}
