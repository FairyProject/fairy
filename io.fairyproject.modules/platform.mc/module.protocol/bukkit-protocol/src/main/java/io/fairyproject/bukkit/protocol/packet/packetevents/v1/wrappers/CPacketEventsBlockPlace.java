package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketBlockPlace;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.translate.PacketEventsTranslators;
import io.fairyproject.mc.protocol.wrapper.mcp.ArtemisDirection;
import io.fairyproject.mc.protocol.wrapper.mcp.ArtemisHand;
import io.fairyproject.mc.protocol.wrapper.vector.Vector3F;
import io.fairyproject.mc.protocol.wrapper.vector.Vector3I;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.utils.player.Direction;
import io.github.retrooper.packetevents.utils.player.Hand;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class CPacketEventsBlockPlace extends PacketEventWrapper<WrappedPacketInBlockPlace> implements CPacketBlockPlace {
    public CPacketEventsBlockPlace(WrappedPacketInBlockPlace wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public ArtemisHand getHand() {
        final Hand bridge = wrapper.getHand();
        return PacketEventsTranslators.HAND.transform(bridge);
    }

    @Override
    public ArtemisDirection getDirection() {
        final Direction bridge = wrapper.getDirection();
        return PacketEventsTranslators.DIRECTION.transform(bridge);
    }

    @Override
    public Vector3I getClickedBlock() {
        final Vector3i bridge = wrapper.getBlockPosition();
        return PacketEventsTranslators.VECTOR_3I.transform(bridge);
    }

    @Override
    public Optional<Vector3F> getClickedOffset() {
        final Optional<Vector3f> optionalBridge = wrapper.getCursorPosition();
        if (!optionalBridge.isPresent())
            return Optional.empty();

        final Vector3f bridge = optionalBridge.get();
        final Vector3F translated = PacketEventsTranslators.VECTOR_3F.transform(bridge);

        return Optional.of(translated);
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        return wrapper.getItemStack();
    }
}
