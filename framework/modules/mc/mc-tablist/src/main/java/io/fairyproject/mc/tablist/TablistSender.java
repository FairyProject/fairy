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

package io.fairyproject.mc.tablist;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.tablist.util.Skin;
import io.fairyproject.mc.version.MCVersion;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TablistSender {

    private final MCVersion newVersion = MCVersion.of(19, 3);

    public void addFakePlayer(Tablist tablist, Collection<TabEntry> tabEntries) {
        final MCPlayer player = tablist.getPlayer();

        if (player.getVersion().isHigherOrEqual(newVersion)) {
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
                    EnumSet.of(
                            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
                            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                    ),
                    tabEntries.stream().map(this::createModern).collect(Collectors.toList())
            );
            MCProtocol.sendPacket(player, packet);
        } else {
            WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                    tabEntries.stream()
                            .map(this::createLegacy)
                            .collect(Collectors.toList())
            );
            MCProtocol.sendPacket(player, packet);
        }

    }

    public void removeFakePlayer(Tablist tablist, Collection<TabEntry> tabEntries) {
        final MCPlayer player = tablist.getPlayer();

        if (player.getVersion().isHigherOrEqual(newVersion)) {
            WrapperPlayServerPlayerInfoRemove packet = new WrapperPlayServerPlayerInfoRemove(
                    tabEntries.stream().map(TabEntry::getUuid).collect(Collectors.toList())
            );
            MCProtocol.sendPacket(player, packet);
        } else {
            WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER,
                    tabEntries.stream()
                            .map(this::createLegacy)
                            .collect(Collectors.toList())
            );
            MCProtocol.sendPacket(player, packet);
        }

    }

    public void updateFakeName(Tablist tablist, TabEntry tabEntry, Component text) {
        if (tabEntry.getText().equals(text))
            return;
        tabEntry.setText(text);

        final MCPlayer player = tablist.getPlayer();

        if (player.getVersion().isHigherOrEqual(newVersion)) {
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
                    Collections.singletonList(createModern(tabEntry))
            );
            MCProtocol.sendPacket(player, packet);
        } else {
            WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME,
                    createLegacy(tabEntry)
            );
            MCProtocol.sendPacket(player, packet);
        }
    }

    public void updateFakeLatency(Tablist tablist, TabEntry tabEntry, int latency) {
        if (tabEntry.getLatency() == latency)
            return;
        tabEntry.setLatency(latency);

        MCPlayer player = tablist.getPlayer();

        if (player.getVersion().isHigherOrEqual(newVersion)) {
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
                    Collections.singletonList(createModern(tabEntry))
            );
            MCProtocol.sendPacket(player, packet);
        } else {
            WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY,
                    createLegacy(tabEntry)
            );
            MCProtocol.sendPacket(player, packet);
        }
    }

    public void updateFakeSkin(Tablist tablist, TabEntry tabEntry, Skin skin) {
        if (tabEntry.getTexture().equals(skin))
            return;

        tabEntry.setTexture(skin);

        this.removeFakePlayer(tablist, Collections.singletonList(tabEntry));
        this.addFakePlayer(tablist, Collections.singletonList(tabEntry));
    }

    public void updateHeaderAndFooter(Tablist tablist, Component header, Component footer) {
        MCPlayer player = tablist.getPlayer();

        WrapperPlayServerPlayerListHeaderAndFooter packet = new WrapperPlayServerPlayerListHeaderAndFooter(
                header,
                footer
        );
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

    private WrapperPlayServerPlayerInfoUpdate.PlayerInfo createModern(TabEntry tabEntry) {
        return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                getGameProfile(tabEntry),
                true,
                tabEntry.getLatency(),
                GameMode.SURVIVAL,
                tabEntry.getText(),
                null
        );
    }

    private WrapperPlayServerPlayerInfo.PlayerData createLegacy(TabEntry tabEntry) {
        return new WrapperPlayServerPlayerInfo.PlayerData(
                tabEntry.getText(),
                getGameProfile(tabEntry),
                GameMode.SURVIVAL,
                tabEntry.getLatency()
        );
    }
}
