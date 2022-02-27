package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.translate.PacketEventsTranslators;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.mcp.ObjectiveActionType;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.item.ObjectiveRenderType;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.server.SPacketScoreboardObjective;
import net.kyori.adventure.text.Component;

import java.util.Optional;

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
        if (!wrapper.getDisplayName().isPresent()) {
            return Component.empty();
        } else {
            final String message = wrapper.getDisplayName().get();
            return MCProtocol.INSTANCE.version().below(MCVersion.V1_13)
                    ? MCAdventure.LEGACY.deserialize(message)
                    : MCAdventure.GSON.deserialize(message);
        }
    }

    @Override
    public Optional<ObjectiveRenderType> getRenderType() {
        return wrapper.getDisplay().map(PacketEventsTranslators.SCOREBOARD_DISPLAY_TYPE::transform);
    }

    @Override
    public ObjectiveActionType getMethod() {
        return PacketEventsTranslators.SCOREBOARD_ACTION_TYPE.transform(wrapper.getMode());
    }
}
