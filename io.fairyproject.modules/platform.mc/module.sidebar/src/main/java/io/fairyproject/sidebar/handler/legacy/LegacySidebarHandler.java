package io.fairyproject.sidebar.handler.legacy;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.sidebar.Sidebar;
import io.fairyproject.sidebar.SidebarLine;
import io.fairyproject.sidebar.handler.AbstractSidebarHandler;
import io.fairyproject.util.CC;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class LegacySidebarHandler extends AbstractSidebarHandler {
    @Override
    public void sendLine(@NotNull Sidebar sidebar, int index, @NotNull SidebarLine line) {
        MCPlayer player = sidebar.getPlayer();
        WrapperPlayServerTeams packet = getOrRegisterTeam(sidebar, index);
        WrapperPlayServerTeams.ScoreBoardTeamInfo info = createSidebarTeamInfo(line.getComponent(), player.getLocale());
        packet.setTeamInfo(info);

        MCProtocol.sendPacket(player, packet);
    }

    public WrapperPlayServerTeams.ScoreBoardTeamInfo createSidebarTeamInfo(Component component, Locale locale) {
        String value = MCAdventure.asLegacyString(component, locale);
        String prefix;
        String suffix;

        if (value.length() <= 16) {
            prefix = value;
            suffix = "";
        } else {
            prefix = value.substring(0, 16);
            String lastColor = CC.getLastColors(prefix);

            if (lastColor.isEmpty() || lastColor.equals(" "))
                lastColor = CC.CODE + "f";

            if (prefix.endsWith(CC.CODE + "")) {
                prefix = prefix.substring(0, 15);
                suffix = lastColor + value.substring(15);

            } else
                suffix = lastColor + value.substring(16);

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }
        }

        return new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                MCAdventure.LEGACY.deserialize(prefix),
                MCAdventure.LEGACY.deserialize(suffix),
                WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                null,
                WrapperPlayServerTeams.OptionData.fromValue((byte) 0)
        );
    }

    @Override
    public void removeLine(@NotNull Sidebar sidebar, int index) {
        MCPlayer player = sidebar.getPlayer();
        PacketWrapper<?> resetScorePacket = getResetScorePacket(sidebar, index);
        WrapperPlayServerTeams teamPacket = getOrRegisterTeam(sidebar, index);
        teamPacket.setTeamMode(WrapperPlayServerTeams.TeamMode.REMOVE);

        MCProtocol.sendPacket(player, resetScorePacket);
        MCProtocol.sendPacket(player, teamPacket);
    }

    protected PacketWrapper<?> getResetScorePacket(Sidebar sidebar, int index) {
        MCPlayer player = sidebar.getPlayer();
        return new WrapperPlayServerUpdateScore(getEntry(index), WrapperPlayServerUpdateScore.Action.REMOVE_ITEM, player.getName(), Optional.empty());
    }

    private WrapperPlayServerTeams getOrRegisterTeam(Sidebar sidebar, int index) {
        MCPlayer player = sidebar.getPlayer();
        if (sidebar.getLine(index) != null) {
            return new WrapperPlayServerTeams("-sb" + index, WrapperPlayServerTeams.TeamMode.UPDATE, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null);
        }

        WrapperPlayServerUpdateScore score = new WrapperPlayServerUpdateScore(this.getEntry(index), WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM, player.getName(), Optional.of(index));
        MCProtocol.sendPacket(player, score);

        return new WrapperPlayServerTeams("-sb" + index, WrapperPlayServerTeams.TeamMode.CREATE, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, getEntry(index));
    }

    protected String getEntry(int line) {
        if (line > 0 && line < 16) {
            if (line <= 10)
                return CC.CODE + "" + (line - 1) + CC.WHITE;

            String values = "a,b,c,d,e,f";
            String[] next = values.split(",");

            return CC.CODE + next[line - 11] + CC.WHITE;
        }
        return "";
    }
}
