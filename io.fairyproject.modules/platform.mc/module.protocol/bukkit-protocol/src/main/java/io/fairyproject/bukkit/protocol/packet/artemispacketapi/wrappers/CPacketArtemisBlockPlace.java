package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.bukkit.protocol.packet.artemispacketapi.translate.ArtemisPacketTranslators;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketBlockPlace;
import cc.ghast.packet.nms.EnumDirection;
import cc.ghast.packet.wrapper.bukkit.BlockPosition;
import cc.ghast.packet.wrapper.bukkit.Vector3D;
import cc.ghast.packet.wrapper.mc.PlayerEnums;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientBlockPlace;
import io.fairyproject.mc.util.Vec3d;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class CPacketArtemisBlockPlace extends ArtemisPacketWrapper<GPacketPlayClientBlockPlace> implements CPacketBlockPlace {
    public CPacketArtemisBlockPlace(GPacketPlayClientBlockPlace wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public Hand getHand() {
        final PlayerEnums.Hand bridge = wrapper.getHand();
        return ArtemisPacketTranslators.HAND.transform(bridge);
    }

    @Override
    public Direction getDirection() {
        final Optional<EnumDirection> bridge = wrapper.getDirection();

        if (!bridge.isPresent()) {
            return Direction.OTHER;
        }

        return ArtemisPacketTranslators.DIRECTION.transform(bridge.get());
    }

    @Override
    public Vec3i getClickedBlock() {
        final BlockPosition bridge = wrapper.getPosition();
        return ArtemisPacketTranslators.VECTOR_3I.transform(bridge);
    }

    @Override
    public Optional<Vec3f> getClickedOffset() {
        final Vector3D optionalBridge = wrapper.getVector();
        final Vec3f translated = ArtemisPacketTranslators.VECTOR_3D.transform(optionalBridge);

        return Optional.of(translated);
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        return wrapper.getItem();
    }
}
