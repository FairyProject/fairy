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

package org.fairy.bukkit.tablist;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.fairy.bukkit.tablist.util.BufferedTabObject;
import org.fairy.bukkit.tablist.util.LegacyClientUtil;
import org.fairy.bukkit.tablist.util.TabColumn;
import org.fairy.bukkit.tablist.util.TabEntry;
import org.fairy.bukkit.util.Skin;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.version.PlayerVersion;
import org.fairy.util.CC;

import java.util.*;

@Getter
public class ImanityTablist {

    private final Player player;
    private final Set<TabEntry> currentEntries = new HashSet<>();

    private String header;
    private String footer;

    public ImanityTablist(Player player) {
        this.player = player;
        this.setup();
    }

    private void setup() {
        final int possibleSlots = MinecraftReflection.getProtocol(player) == PlayerVersion.v1_7 ? 60 : 80;

        for (int i = 1; i <= possibleSlots; i++) {
            final TabColumn tabColumn = TabColumn.getFromSlot(player, i);
            if (tabColumn == null) {
                continue;
            }

            TabEntry tabEntry = ImanityTabHandler.getInstance().getImplementation().createFakePlayer(
                    this,
                    "0" + (i > 9 ? i : "0" + i) + "|Tab",
                    tabColumn,
                    tabColumn.getNumb(player, i),
                    i
            );
            if (MinecraftReflection.getProtocol(player) == PlayerVersion.v1_7) {

                Imanity.IMPLEMENTATION.sendTeam(
                        player,
                        LegacyClientUtil.name(i - 1),
                        "",
                        "",
                        Collections.singleton(LegacyClientUtil.entry(i - 1)),
                        0
                );
            }
            currentEntries.add(tabEntry);
        }
    }

    public void update() {
        ImanityTabAdapter adapter = ImanityTabHandler.getInstance().getAdapter();

        Set<TabEntry> previous = new HashSet<>(currentEntries);

        Set<BufferedTabObject> processedObjects = adapter.getSlots(player);
        if (processedObjects == null) {
            processedObjects = new HashSet<>();
        }

        for (BufferedTabObject scoreObject : processedObjects) {
            TabEntry tabEntry = getEntry(scoreObject.getColumn(), scoreObject.getSlot());
            if (tabEntry != null) {
                previous.remove(tabEntry);
                if (scoreObject.getPing() == null) {
                    ImanityTabHandler.getInstance().getImplementation().updateFakeLatency(this, tabEntry, 0);
                } else {
                    ImanityTabHandler.getInstance().getImplementation().updateFakeLatency(this, tabEntry, scoreObject.getPing());
                }

                ImanityTabHandler.getInstance().getImplementation().updateFakeName(this, tabEntry, scoreObject.getText());
                if (MinecraftReflection.getProtocol(player) != PlayerVersion.v1_7) {
                    if (!tabEntry.getTexture().toString().equals(scoreObject.getSkin().toString())) {
                        ImanityTabHandler.getInstance().getImplementation().updateFakeSkin(this, tabEntry, scoreObject.getSkin());
                    }
                }
            }
        }

        for (TabEntry tabEntry : previous) {
            ImanityTabHandler.getInstance().getImplementation().updateFakeName(this, tabEntry, "");
            ImanityTabHandler.getInstance().getImplementation().updateFakeLatency(this, tabEntry, 0);
            if (MinecraftReflection.getProtocol(player) != PlayerVersion.v1_7) {
                ImanityTabHandler.getInstance().getImplementation().updateFakeSkin(this, tabEntry, Skin.GRAY);
            }
        }

        previous.clear();

        String headerNow = CC.translate(adapter.getHeader(player));
        String footerNow = CC.translate(adapter.getFooter(player));

        if (!headerNow.equals(this.header) || !footerNow.equals(this.footer)) {
            ImanityTabHandler.getInstance().getImplementation().updateHeaderAndFooter(this, headerNow, footerNow);
            this.header = headerNow;
            this.footer = footerNow;
        }
    }

    public TabEntry getEntry(TabColumn column, Integer slot){
        for (TabEntry entry : currentEntries){
            if (entry.getColumn().name().equalsIgnoreCase(column.name()) && entry.getSlot() == slot){
                return entry;
            }
        }
        return null;
    }

    public static String[] splitStrings(String text, int rawSlot) {
        if (text.length() > 16) {
            String prefix = text.substring(0, 16);
            String suffix;

            if (prefix.charAt(15) == ChatColor.COLOR_CHAR || prefix.charAt(15) == '&') {
                prefix = prefix.substring(0, 15);
                suffix = text.substring(15);
            } else if (prefix.charAt(14) == ChatColor.COLOR_CHAR || prefix.charAt(14) == '&') {
                prefix = prefix.substring(0, 14);
                suffix = text.substring(14);
            } else {
                suffix = ChatColor.getLastColors(ChatColor.translateAlternateColorCodes('&',prefix)) + text.substring(16, text.length());
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            //Bukkit.broadcastMessage(prefix + " |||| " + suffix);
            return new String[] {
                    prefix,
                    suffix
            };
        } else {
            return new String[] {
                    text
            };
        }
    }
}
