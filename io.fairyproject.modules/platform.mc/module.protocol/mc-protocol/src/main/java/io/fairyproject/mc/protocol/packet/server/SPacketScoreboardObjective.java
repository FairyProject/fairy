package io.fairyproject.mc.protocol.packet.server;

import io.fairyproject.mc.mcp.ObjectiveActionType;
import io.fairyproject.mc.protocol.item.ObjectiveRenderType;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public interface SPacketScoreboardObjective extends SPacket {
    String getObjectiveName();

    Component getDisplayName();

    Optional<ObjectiveRenderType> getRenderType();

    ObjectiveActionType getMethod();

    @Override
    default String getFancyName() {
        return "ScoreboardObjective";
    }
}
