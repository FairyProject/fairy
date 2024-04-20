package io.fairyproject.sidebar.handler.legacy;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public class V13LegacySidebarHandler extends LegacySidebarHandler {
    @Override
    public WrapperPlayServerTeams.ScoreBoardTeamInfo createSidebarTeamInfo(Component component, Locale locale) {
        return new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                component,
                Component.empty(),
                WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                null,
                WrapperPlayServerTeams.OptionData.fromValue((byte) 0)
        );
    }
}
