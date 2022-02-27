package io.fairyproject.mc.protocol.packet.server;

import io.fairyproject.mc.protocol.item.ObjectiveRenderType;
import net.kyori.adventure.text.Component;

public interface SPacketScoreboardObjective extends SPacket {
    String getObjectiveName();

    Component getDisplayName();

    ObjectiveRenderType getRenderType();

    int getMethod();

    @Override
    default String getFancyName() {
        return "ScoreboardObjective";
    }
}
