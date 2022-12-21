/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.mc.tablist.util;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.packet.PacketPlay;
import io.fairyproject.mc.tablist.TabColumn;
import io.fairyproject.mc.tablist.TabEntry;
import io.fairyproject.mc.tablist.Tablist;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class TablistUtil {

    public TabEntry createFakePlayer(Tablist tablist, String string, TabColumn column, int slot, int rawSlot) {
        final MCPlayer player = tablist.getPlayer();

        TabEntry tabEntry = new TabEntry(string, UUID.randomUUID(), Component.empty(), tablist, Skin.GRAY, column, slot, 0);
        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                create(tabEntry)
        );

        MCProtocol.sendPacket(player, packet);
        return tabEntry;
    }

    public void addFakePlayer(Tablist tablist, Collection<TabEntry> tabEntries) {
        final MCPlayer player = tablist.getPlayer();

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                tabEntries.stream()
                        .map(TablistUtil::create)
                        .collect(Collectors.toList())
        );

        MCProtocol.sendPacket(player, packet);
    }

    public void removeFakePlayer(Tablist tablist, Collection<TabEntry> tabEntries) {
        final MCPlayer player = tablist.getPlayer();

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER,
                tabEntries.stream()
                        .map(TablistUtil::create)
                        .collect(Collectors.toList())
        );

        MCProtocol.sendPacket(player, packet);
    }

    public void updateFakeName(Tablist tablist, TabEntry tabEntry, Component text) {
        if (tabEntry.getText().equals(text))
            return;

        final MCPlayer player = tablist.getPlayer();

        tabEntry.setText(text);

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME,
                create(tabEntry)
        );

        MCProtocol.sendPacket(player, packet);
    }

    public void updateFakeLatency(Tablist tablist, TabEntry tabEntry, int latency) {
        if (tabEntry.getLatency() == latency)
            return;

        tabEntry.setLatency(latency);

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY,
                create(tabEntry)
        );
        MCProtocol.sendPacket(tablist.getPlayer(), packet);
    }

    public void updateFakeSkin(Tablist tablist, TabEntry tabEntry, Skin skin) {
        if (tabEntry.getTexture().equals(skin))
            return;

        tabEntry.setTexture(skin);

        WrapperPlayServerPlayerInfo.PlayerData playerData = create(tabEntry);
        WrapperPlayServerPlayerInfo remove = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER,
                playerData
        );
        WrapperPlayServerPlayerInfo add = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                playerData
        );

        MCProtocol.sendPacket(tablist.getPlayer(), remove);
        MCProtocol.sendPacket(tablist.getPlayer(), add);
    }

    public void updateHeaderAndFooter(Tablist tablist, Component header, Component footer) {
        MCPlayer player = tablist.getPlayer();

        final PacketPlay.Out.Tablist packet = PacketPlay.Out.Tablist.builder()
                .header(header)
                .footer(footer)
                .build();
        MCProtocol.sendPacket(player, packet);
    }

    private UserProfile getGameProfile(TabEntry tabEntry) {
        UserProfile userProfile = new UserProfile(tabEntry.getUuid(), tabEntry.getId());
        if (tabEntry.getTexture() != null) {
            userProfile.setTextureProperties(Collections.singletonList(new TextureProperty(
                    "textures",
                    tabEntry.getTexture().skinValue,
                    tabEntry.getTexture().skinSignature
            )));
        }
        return userProfile;
    }

    private WrapperPlayServerPlayerInfo.PlayerData create(TabEntry tabEntry) {
        return new WrapperPlayServerPlayerInfo.PlayerData(
                tabEntry.getText(),
                getGameProfile(tabEntry),
                GameMode.SURVIVAL,
                tabEntry.getLatency()
        );
    }
}
