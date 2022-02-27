package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.translate.PacketEventsTranslationHelper;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketEntityAction;

public class CPacketEventsEntityAction extends PacketEventWrapper<WrapperPlayClientEntityAction> implements CPacketEntityAction {
    public CPacketEventsEntityAction(WrapperPlayClientEntityAction wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public PlayerAction getAction() {
        final WrapperPlayClientEntityAction.Action bridge = wrapper.getAction();
        return bridge == null ? null : PacketEventsTranslationHelper.PLAYER_ACTION.transform(bridge);
    }
}
