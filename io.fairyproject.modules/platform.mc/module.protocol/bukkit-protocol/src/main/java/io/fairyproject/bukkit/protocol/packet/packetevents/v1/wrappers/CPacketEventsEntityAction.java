package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.translate.PacketEventsTranslationHelper;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketEntityAction;
import io.github.retrooper.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;

public class CPacketEventsEntityAction extends PacketEventWrapper<WrappedPacketInEntityAction> implements CPacketEntityAction {
    public CPacketEventsEntityAction(WrappedPacketInEntityAction wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public PlayerAction getAction() {
        final WrappedPacketInEntityAction.PlayerAction bridge = wrapper.getAction();
        return bridge == null ? null : PacketEventsTranslationHelper.PLAYER_ACTION.transform(bridge);
    }
}
