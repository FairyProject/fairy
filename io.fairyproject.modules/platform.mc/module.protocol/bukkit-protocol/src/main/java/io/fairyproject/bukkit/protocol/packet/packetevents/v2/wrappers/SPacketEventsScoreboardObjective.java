package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.item.ObjectiveRenderType;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.server.SPacketScoreboardObjective;
import net.kyori.adventure.text.Component;

public class SPacketEventsScoreboardObjective extends PacketEventWrapper<WrapperPlayServerScoreboardObjective> implements SPacketScoreboardObjective {
    public SPacketEventsScoreboardObjective(WrapperPlayServerScoreboardObjective wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public String getObjectiveName() {
        return wrapper.getName();
    }

    @Override
    public Component getDisplayName() {
        return wrapper.getDisplay();
    }

    @Override
    public ObjectiveRenderType getRenderType() {
        return null;
    }

    @Override
    public int getMethod() {
        return 0;
    }
}
