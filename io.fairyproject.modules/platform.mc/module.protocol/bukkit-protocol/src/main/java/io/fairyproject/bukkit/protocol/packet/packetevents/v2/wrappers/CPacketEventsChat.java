package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventV2Wrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketChat;

public class CPacketEventsChat extends PacketEventV2Wrapper<WrapperPlayClientChatMessage> implements CPacketChat {
    public CPacketEventsChat(WrapperPlayClientChatMessage wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public String getMessage() {
        return wrapper.getMessage();
    }
}
