package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.translate.PacketEventsTranslationHelper;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketBlockPlace;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class CPacketEventsBlockPlace extends PacketEventWrapper<WrapperPlayClientPlayerBlockPlacement> implements CPacketBlockPlace {
    public CPacketEventsBlockPlace(WrapperPlayClientPlayerBlockPlacement wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public Hand getHand() {
        final InteractionHand bridge = wrapper.getHand();
        return PacketEventsTranslationHelper.HAND.transform(bridge);
    }

    @Override
    public Direction getDirection() {
        final BlockFace bridge = wrapper.getFace();
        return PacketEventsTranslationHelper.DIRECTION.transform(bridge);
    }

    @Override
    public Vec3i getClickedBlock() {
        final Vector3i bridge = wrapper.getBlockPosition();
        return PacketEventsTranslationHelper.VECTOR_3I.transform(bridge);
    }

    @Override
    public Optional<Vec3f> getClickedOffset() {
        final Vector3f optionalBridge = wrapper.getCursorPosition();
        if (optionalBridge == null)
            return Optional.empty();

        final Vec3f translated = PacketEventsTranslationHelper.VECTOR_3F.transform(optionalBridge);

        return Optional.of(translated);
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        // TODO: ItemWrapper to ItemStack conversion
        throw new IllegalStateException("Todo!");
    }
}
