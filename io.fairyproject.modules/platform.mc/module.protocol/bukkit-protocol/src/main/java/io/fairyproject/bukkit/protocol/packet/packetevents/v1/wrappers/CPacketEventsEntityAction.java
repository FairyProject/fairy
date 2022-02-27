package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketEntityAction;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.translate.PacketEventsTranslators;
import io.fairyproject.mc.protocol.wrapper.mcp.ArtemisPlayerAction;
import io.github.retrooper.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;

public class CPacketEventsEntityAction extends PacketEventWrapper<WrappedPacketInEntityAction> implements CPacketEntityAction {
    public CPacketEventsEntityAction(WrappedPacketInEntityAction wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public ArtemisPlayerAction getAction() {
        final WrappedPacketInEntityAction.PlayerAction bridge = wrapper.getAction();
        return bridge == null ? null : PacketEventsTranslators.PLAYER_ACTION.transform(bridge);
    }
}
