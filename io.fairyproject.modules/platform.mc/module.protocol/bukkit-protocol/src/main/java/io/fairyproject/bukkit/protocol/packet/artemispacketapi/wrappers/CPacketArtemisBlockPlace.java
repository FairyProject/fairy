package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.bukkit.protocol.packet.artemispacketapi.translate.ArtemisPacketTranslationHelper;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketBlockPlace;
import cc.ghast.packet.nms.EnumDirection;
import cc.ghast.packet.wrapper.bukkit.BlockPosition;
import cc.ghast.packet.wrapper.bukkit.Vector3D;
import cc.ghast.packet.wrapper.mc.PlayerEnums;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientBlockPlace;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class CPacketArtemisBlockPlace extends ArtemisPacketWrapper<GPacketPlayClientBlockPlace> implements CPacketBlockPlace {
    public CPacketArtemisBlockPlace(GPacketPlayClientBlockPlace wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public Hand getHand() {
        final PlayerEnums.Hand bridge = wrapper.getHand();
        return ArtemisPacketTranslationHelper.HAND.transform(bridge);
    }

    @Override
    public Direction getDirection() {
        final Optional<EnumDirection> bridge = wrapper.getDirection();

        if (!bridge.isPresent()) {
            return Direction.OTHER;
        }

        return ArtemisPacketTranslationHelper.DIRECTION.transform(bridge.get());
    }

    @Override
    public Vec3i getClickedBlock() {
        final BlockPosition bridge = wrapper.getPosition();
        return ArtemisPacketTranslationHelper.VECTOR_3I.transform(bridge);
    }

    @Override
    public Optional<Vec3f> getClickedOffset() {
        final Vector3D optionalBridge = wrapper.getVector();
        final Vec3f translated = ArtemisPacketTranslationHelper.VECTOR_3D.transform(optionalBridge);

        return Optional.of(translated);
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        return wrapper.getItem();
    }
}
