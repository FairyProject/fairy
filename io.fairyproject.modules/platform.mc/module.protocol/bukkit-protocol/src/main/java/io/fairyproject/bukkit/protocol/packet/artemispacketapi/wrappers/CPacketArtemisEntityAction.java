package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketEntityAction;
import io.fairyproject.mc.protocol.spigot.packet.artemispacketapi.translate.ArtemisPacketTranslators;
import io.fairyproject.mc.protocol.spigot.packet.packetevents.PacketEventWrapper;
import io.fairyproject.mc.protocol.wrapper.mcp.ArtemisPlayerAction;
import cc.ghast.packet.wrapper.mc.PlayerEnums;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientEntityAction;

public class CPacketArtemisEntityAction extends PacketEventWrapper<GPacketPlayClientEntityAction> implements CPacketEntityAction {
    public CPacketArtemisEntityAction(GPacketPlayClientEntityAction wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public ArtemisPlayerAction getAction() {
        final PlayerEnums.PlayerAction bridge = wrapper.getAction();
        return bridge == null ? null : ArtemisPacketTranslators.PLAYER_ACTION.transform(bridge);
    }
}
