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

import io.fairyproject.Fairy;
import net.kyori.adventure.text.Component;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.item.*;
import io.fairyproject.mc.protocol.packet.PacketPlay;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.util.CC;

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

        player.sendPacket(PacketPlay.Out.ScoreboardObjective.builder()
                .objectiveName(player.getName())
                .displayName(Component.text("Objective"))
                .renderType(ObjectiveRenderType.INTEGER)
                .method(0)
                .build());

        player.sendPacket(PacketPlay.Out.ScoreboardDisplayObjective.builder()
                .displaySlot(ObjectiveDisplaySlot.SIDEBAR)
                .objectiveName(player.getName())
                .build());

    }

    public void setTitle(Component title) {
        if (this.title != null && this.title.equals(title)) {
            return;
        }

        this.title = title;
        player.sendPacket(PacketPlay.Out.ScoreboardObjective.builder()
                .objectiveName(player.getName())
                .displayName(title)
                .renderType(ObjectiveRenderType.INTEGER)
                .method(2)
                .build());
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

        final String value = MCAdventure.asLegacyString(component, this.player.getLocale());
        final PacketPlay.Out.ScoreboardTeam packet = getOrRegisterTeam(line);
        PacketPlay.Out.ScoreboardTeam.Parameters.ParametersBuilder builder = PacketPlay.Out.ScoreboardTeam.Parameters.builder();
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

        builder.playerPrefix(MCAdventure.LEGACY.deserialize(prefix));
        builder.playerSuffix(MCAdventure.LEGACY.deserialize(suffix));

        teams[line] = component;

        packet.setParameters(Optional.of(builder.build()));
        this.player.sendPacket(packet);
    }

    public void clear(int line) {
        if (line > 0 && line < 16 && teams[line] != null) {
            final PacketPlay.Out.ScoreboardScore packetA = PacketPlay.Out.ScoreboardScore.builder()
                    .owner(this.getEntry(line))
                    .objectiveName(player.getName())
                    .score(line)
                    .action(ScoreAction.REMOVE)
                    .build();
            final PacketPlay.Out.ScoreboardTeam packetB = getOrRegisterTeam(line);
            packetB.setTeamAction(TeamAction.REMOVE);

            teams[line] = null;

            player.sendPacket(packetA);
            player.sendPacket(packetB);
        }
    }

    public void remove() {
        for (int line = 1; line < 15; line++) {
            this.clear(line);
        }
    }

    private PacketPlay.Out.ScoreboardTeam getOrRegisterTeam(int line) {
        final PacketPlay.Out.ScoreboardTeam.Factory builder = PacketPlay.Out.ScoreboardTeam.builder();
        builder.name("-sb" + line);
        builder.teamAction(TeamAction.ADD);

        if (teams[line] != null) {
            builder.teamAction(TeamAction.CHANGE);

            return builder.build();
        } else {
            teams[line] = null;

            final PacketPlay.Out.ScoreboardScore score = PacketPlay.Out.ScoreboardScore.builder()
                    .owner(this.getEntry(line))
                    .objectiveName(player.getName())
                    .score(line)
                    .action(ScoreAction.CHANGE)
                    .build();

            builder.teamAction(TeamAction.ADD);
            builder.players(getEntry(line));
            this.player.sendPacket(score);

            return builder.build();
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
