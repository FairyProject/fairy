package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.bukkit.protocol.packet.artemispacketapi.translate.ArtemisPacketTranslationHelper;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketEntityAction;
import cc.ghast.packet.wrapper.mc.PlayerEnums;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientEntityAction;

public class CPacketArtemisEntityAction extends ArtemisPacketWrapper<GPacketPlayClientEntityAction> implements CPacketEntityAction {
    public CPacketArtemisEntityAction(GPacketPlayClientEntityAction wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public PlayerAction getAction() {
        final PlayerEnums.PlayerAction bridge = wrapper.getAction();
        return bridge == null ? null : ArtemisPacketTranslationHelper.PLAYER_ACTION.transform(bridge);
    }
}
