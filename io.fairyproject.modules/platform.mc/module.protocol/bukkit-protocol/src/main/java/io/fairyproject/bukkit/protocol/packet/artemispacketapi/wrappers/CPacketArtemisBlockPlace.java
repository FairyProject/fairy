package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketBlockPlace;
import io.fairyproject.mc.protocol.spigot.packet.artemispacketapi.translate.ArtemisPacketTranslators;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.fairyproject.mc.protocol.wrapper.mcp.ArtemisDirection;
import io.fairyproject.mc.protocol.wrapper.mcp.ArtemisHand;
import io.fairyproject.mc.protocol.wrapper.vector.Vector3F;
import io.fairyproject.mc.protocol.wrapper.vector.Vector3I;
import cc.ghast.packet.nms.EnumDirection;
import cc.ghast.packet.wrapper.bukkit.BlockPosition;
import cc.ghast.packet.wrapper.bukkit.Vector3D;
import cc.ghast.packet.wrapper.mc.PlayerEnums;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientBlockPlace;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class CPacketArtemisBlockPlace extends PacketEventWrapper<GPacketPlayClientBlockPlace> implements CPacketBlockPlace {
    public CPacketArtemisBlockPlace(GPacketPlayClientBlockPlace wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public ArtemisHand getHand() {
        final PlayerEnums.Hand bridge = wrapper.getHand();
        return ArtemisPacketTranslators.HAND.transform(bridge);
    }

    @Override
    public ArtemisDirection getDirection() {
        final Optional<EnumDirection> bridge = wrapper.getDirection();

        if (!bridge.isPresent()) {
            return ArtemisDirection.OTHER;
        }

        return ArtemisPacketTranslators.DIRECTION.transform(bridge.get());
    }

    @Override
    public Vector3I getClickedBlock() {
        final BlockPosition bridge = wrapper.getPosition();
        return ArtemisPacketTranslators.VECTOR_3I.transform(bridge);
    }

    @Override
    public Optional<Vector3F> getClickedOffset() {
        final Vector3D optionalBridge = wrapper.getVector();
        final Vector3F translated = ArtemisPacketTranslators.VECTOR_3D.transform(optionalBridge);

        return Optional.of(translated);
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        return wrapper.getItem();
    }
}
