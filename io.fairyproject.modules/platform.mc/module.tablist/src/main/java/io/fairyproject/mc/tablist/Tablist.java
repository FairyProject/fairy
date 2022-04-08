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

package io.fairyproject.mc.tablist;

import io.fairyproject.container.Autowired;
import net.kyori.adventure.text.Component;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.PacketPlay;
import io.fairyproject.mc.tablist.util.*;
import lombok.Getter;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.util.CC;

import java.util.*;

@Getter
public class Tablist {

    @Autowired
    private static TablistService SERVICE;

    private final MCPlayer player;
    private final Set<TabEntry> currentEntries = new HashSet<>();

    private Component header;
    private Component footer;

    public Tablist(MCPlayer player) {
        this.player = player;
        this.setup();
    }

    private void setup() {
        final int possibleSlots = player.getVersion() == MCVersion.V1_7 ? 60 : 80;

        for (int i = 1; i <= possibleSlots; i++) {
            final TabColumn tabColumn = TabColumn.getFromSlot(player, i);
            if (tabColumn == null) {
                continue;
            }

            TabEntry tabEntry = SERVICE.getImplementation().createFakePlayer(
                    this,
                    String.format("%03d", i) + "|Tab",
                    tabColumn,
                    tabColumn.getNumb(player, i),
                    i
            );
            if (player.getVersion() == MCVersion.V1_7) {
                final PacketPlay.Out.ScoreboardTeam packet = PacketPlay.Out.ScoreboardTeam.builder()
                        .name(LegacyClientUtil.name(i - 1))
                        .parameters(Optional.of(PacketPlay.Out.ScoreboardTeam.Parameters.builder()
                                .playerPrefix(Component.empty())
                                .playerSuffix(Component.empty())
                                .build()))
                        .players(LegacyClientUtil.entry(i - 1))
                        .build();

                player.sendPacket(packet);
            }
            currentEntries.add(tabEntry);
        }
    }

    public void update() {
        Set<TabEntry> previous = new HashSet<>(currentEntries);

        Set<TabSlot> processedObjects = SERVICE.getSlots(player);
        if (processedObjects == null) {
            processedObjects = Collections.emptySet();
        }

        for (TabSlot scoreObject : processedObjects) {
            TabEntry tabEntry = getEntry(scoreObject.getColumn(), scoreObject.getSlot());
            if (tabEntry != null) {
                previous.remove(tabEntry);
                SERVICE.getImplementation().updateFakeLatency(this, tabEntry, scoreObject.getPing());
                SERVICE.getImplementation().updateFakeName(this, tabEntry, scoreObject.getText());
                if (player.getVersion() != MCVersion.V1_7) {
                    if (!tabEntry.getTexture().toString().equals(scoreObject.getSkin().toString())) {
                        SERVICE.getImplementation().updateFakeSkin(this, tabEntry, scoreObject.getSkin());
                    }
                }
            }
        }

        for (TabEntry tabEntry : previous) {
            SERVICE.getImplementation().updateFakeName(this, tabEntry, Component.empty());
            SERVICE.getImplementation().updateFakeLatency(this, tabEntry, 0);
            if (player.getVersion() != MCVersion.V1_7) {
                SERVICE.getImplementation().updateFakeSkin(this, tabEntry, Skin.GRAY);
            }
        }

        previous.clear();

        Component headerNow = SERVICE.getHeader(player);
        Component footerNow = SERVICE.getFooter(player);

        if (!Objects.equals(this.header, headerNow) || !Objects.equals(this.footer, footerNow)) {
            SERVICE.getImplementation().updateHeaderAndFooter(this, headerNow, footerNow);
            this.header = headerNow;
            this.footer = footerNow;
        }
    }

    public TabEntry getEntry(TabColumn column, Integer slot) {
        for (TabEntry entry : currentEntries) {
            if (entry.getColumn().name().equalsIgnoreCase(column.name()) && entry.getSlot() == slot) {
                return entry;
            }
        }
        return null;
    }

    public static String[] splitStrings(String text, int rawSlot) {
        if (text.length() > 16) {
            String prefix = text.substring(0, 16);
            String suffix;

            if (prefix.charAt(15) == CC.CODE || prefix.charAt(15) == '&') {
                prefix = prefix.substring(0, 15);
                suffix = text.substring(15);
            } else if (prefix.charAt(14) == CC.CODE || prefix.charAt(14) == '&') {
                prefix = prefix.substring(0, 14);
                suffix = text.substring(14);
            } else {
                suffix = CC.getLastColors(CC.translate(prefix)) + text.substring(16);
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            //Bukkit.broadcastMessage(prefix + " |||| " + suffix);
            return new String[]{
                    prefix,
                    suffix
            };
        } else {
            return new String[]{
                    text
            };
        }
    }
}
