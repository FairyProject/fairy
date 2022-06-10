/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.sidebar;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import io.fairyproject.Fairy;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.item.ObjectiveDisplaySlot;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.util.CC;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public class Sidebar {

    public static final MetadataKey<Sidebar> METADATA_TAG = MetadataKey.create(Fairy.METADATA_PREFIX + "Scoreboard", Sidebar.class);

    private final MCPlayer player;

    private Component title;
    private final Component[] teams;

    public Sidebar(MCPlayer player) {
        this.player = player;
        this.teams = new Component[16];

        final WrapperPlayServerScoreboardObjective objective = new WrapperPlayServerScoreboardObjective(
                player.getName(),
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                Optional.of(MCServer.current().getVersion().isOrBelow(MCVersion.V1_7)
                        ? MCAdventure.asLegacyString(Component.empty(), player.getLocale())
                        : MCAdventure.asJsonString(Component.empty(), player.getLocale())),
                Optional.of(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER)
        );
        MCProtocol.sendPacket(player, objective);

        final WrapperPlayServerDisplayScoreboard scoreboard = new WrapperPlayServerDisplayScoreboard(
                ObjectiveDisplaySlot.SIDEBAR.getSerializeId(),
                player.getName()
        );

        MCProtocol.sendPacket(player, scoreboard);
    }

    public void setTitle(Component title) {
        if (this.title != null && this.title.equals(title)) {
            return;
        }

        this.title = title;

        MCProtocol.sendPacket(player, new WrapperPlayServerScoreboardObjective(
                player.getName(),
                WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE,
                Optional.of(
                        MCServer.current().getVersion().isOrBelow(MCVersion.V1_12)
                        ? MCAdventure.asLegacyString(title, player.getLocale())
                        : MCAdventure.asJsonString(title, player.getLocale())
                ),
                Optional.of(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER)
        ));
    }

    public void setLines(List<Component> lines) {
        int lineCount = 1;

        for (int i = lines.size() - 1; i >= 0; --i) {
            this.setLine(lineCount, lines.get(i));
            lineCount++;
        }

        for (int i = lines.size(); i < 15; i++) {
            if (teams[lineCount] != null) {
                this.clear(lineCount);
            }
            lineCount++;
        }
    }

    private void setLine(int line, Component component) {
        if (line <= 0 || line >= 16) {
            return;
        }

        if (teams[line] != null && teams[line].equals(component)) {
            return;
        }

        final Object channel = MCProtocol.INSTANCE.getPacketEvents().getProtocolManager().getChannel(player.getUUID());
        final ClientVersion version = MCProtocol.INSTANCE.getPacketEvents().getProtocolManager().getClientVersion(channel);
        final WrapperPlayServerTeams packet = getOrRegisterTeam(line);

        WrapperPlayServerTeams.ScoreBoardTeamInfo info;

        if (version.isNewerThanOrEquals(ClientVersion.V_1_13)) {
            info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                    Component.empty(),
                    component,
                    Component.empty(),
                    WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                    WrapperPlayServerTeams.CollisionRule.ALWAYS,
                    null,
                    WrapperPlayServerTeams.OptionData.fromValue((byte) 0)
            );
        } else {
            final String value = MCAdventure.asLegacyString(component, this.player.getLocale());
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

            info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                    Component.empty(),
                    MCAdventure.LEGACY.deserialize(prefix),
                    MCAdventure.LEGACY.deserialize(suffix),
                    WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                    WrapperPlayServerTeams.CollisionRule.ALWAYS,
                    null,
                    WrapperPlayServerTeams.OptionData.fromValue((byte) 0)
            );
        }

        teams[line] = component;
        packet.setTeamInfo(Optional.of(info));
        MCProtocol.sendPacket(player, packet);
    }

    public void clear(int line) {
        if (line > 0 && line < 16 && teams[line] != null) {
            final WrapperPlayServerUpdateScore packetA = new WrapperPlayServerUpdateScore(
                    this.getEntry(line),
                    WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                    player.getName(),
                    Optional.of(line)
            );
            final WrapperPlayServerTeams packetB = getOrRegisterTeam(line);
            packetB.setTeamMode(WrapperPlayServerTeams.TeamMode.REMOVE);

            teams[line] = null;

            MCProtocol.sendPacket(player, packetA);
            MCProtocol.sendPacket(player, packetB);
        }
    }

    public void remove() {
        for (int line = 1; line < 15; line++) {
            this.clear(line);
        }
    }

    private WrapperPlayServerTeams getOrRegisterTeam(int line) {
        if (teams[line] != null) {
            return new WrapperPlayServerTeams(
                    "-sb" + line,
                    WrapperPlayServerTeams.TeamMode.UPDATE,
                    Optional.empty()
            );
        } else {
            teams[line] = null;

            final WrapperPlayServerUpdateScore score = new WrapperPlayServerUpdateScore(
                    this.getEntry(line),
                    WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                    player.getName(),
                    Optional.of(line)
            );
            MCProtocol.sendPacket(player, score);

            return new WrapperPlayServerTeams(
                    "-sb" + line,
                    WrapperPlayServerTeams.TeamMode.CREATE,
                    Optional.empty(),
                    getEntry(line)
            );
        }

    }

    private String getEntry(Integer line) {
        if (line > 0 && line < 16)
            if (line <= 10)
                return CC.CODE + "" + (line - 1) + CC.WHITE;
            else {
                final String values = "a,b,c,d,e,f";
                final String[] next = values.split(",");

                return CC.CODE + next[line - 11] + CC.WHITE;
            }
        return "";
    }

}
