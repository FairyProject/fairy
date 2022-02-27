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

package io.fairyproject.mc.tablist.util.impl;

import net.kyori.adventure.text.Component;
import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.item.PlayerInfoAction;
import io.fairyproject.mc.protocol.item.PlayerInfoData;
import io.fairyproject.mc.protocol.item.TeamAction;
import io.fairyproject.mc.protocol.packet.PacketPlay;
import io.fairyproject.mc.tablist.util.*;
import io.fairyproject.mc.util.Property;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.tablist.Tablist;
import io.fairyproject.util.CC;

import java.util.Optional;
import java.util.UUID;

public class MainTablistImpl implements TablistImpl {

    @Override
    public void removeSelf(MCPlayer player) {
        final PacketPlay.Out.PlayerInfo playerInfo = PacketPlay.Out.PlayerInfo.builder()
                .action(io.fairyproject.mc.protocol.item.PlayerInfoAction.REMOVE_PLAYER)
                .entries(MCPlayer.from(player).asInfoData())
                .build();

        MCPlayer.all().forEach(mcPlayer -> mcPlayer.sendPacket(playerInfo));
    }

    @Override
    public void registerLoginListener() {

    }

    @Override
    public TabEntry createFakePlayer(Tablist tablist, String string, TabColumn column, int slot, int rawSlot) {
        final MCPlayer player = tablist.getPlayer();
        final MCVersion version = player.getVersion();

        MCGameProfile profile = MCGameProfile.create(UUID.randomUUID(), version != MCVersion.V1_7 ? string : LegacyClientUtil.entry(rawSlot - 1) + "");

        if (version != MCVersion.V1_7) {
            profile.setProperty(new Property("textures", Skin.GRAY.skinValue, Skin.GRAY.skinSignature));
        }

        final PacketPlay.Out.PlayerInfo packet = PacketPlay.Out.PlayerInfo.builder()
                .action(io.fairyproject.mc.protocol.item.PlayerInfoAction.ADD_PLAYER)
                .entries(new PlayerInfoData(1, profile, io.fairyproject.mc.GameMode.SURVIVAL, Component.empty()))
                .build();
        player.sendPacket(packet);
        return new TabEntry(string, profile.getUuid(), Component.empty(), tablist, Skin.GRAY, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(Tablist tablist, TabEntry tabEntry, Component text) {
        if (tabEntry.getText().equals(text)) {
            return;
        }

        final MCPlayer player = tablist.getPlayer();
        final MCVersion version = player.getVersion();

        if (version == MCVersion.V1_7) {

            String[] newStrings = Tablist.splitStrings(MCAdventure.asLegacyString(text, player.getLocale()), tabEntry.getRawSlot());
            final PacketPlay.Out.ScoreboardTeam packet = PacketPlay.Out.ScoreboardTeam.builder()
                    .name(LegacyClientUtil.name(tabEntry.getRawSlot() - 1))
                    .parameters(Optional.of(PacketPlay.Out.ScoreboardTeam.Parameters.builder()
                            .playerPrefix(MCAdventure.LEGACY.deserialize(newStrings[0]))
                            .playerPrefix(MCAdventure.LEGACY.deserialize(newStrings.length > 1 ? CC.translate(newStrings[1]) : ""))
                            .build()))
                    .players(LegacyClientUtil.entry(tabEntry.getRawSlot() - 1))
                    .teamAction(TeamAction.CHANGE)
                    .build();

            player.sendPacket(packet);
        } else {
            final PacketPlay.Out.PlayerInfo packet = PacketPlay.Out.PlayerInfo.builder()
                    .action(PlayerInfoAction.UPDATE_DISPLAY_NAME)
                    .entries(new PlayerInfoData(tabEntry.getLatency(), this.getGameProfile(version, tabEntry), GameMode.SURVIVAL, text))
                    .build();

            player.sendPacket(packet);
        }

        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(Tablist tablist, TabEntry tabEntry, int latency) {
        if (tabEntry.getLatency() == latency) return;

        final MCVersion version = tablist.getPlayer().getVersion();
        final PacketPlay.Out.PlayerInfo packet = PacketPlay.Out.PlayerInfo.builder()
                .action(PlayerInfoAction.UPDATE_LATENCY)
                .entries(new PlayerInfoData(tabEntry.getLatency(), this.getGameProfile(version, tabEntry), GameMode.SURVIVAL, tabEntry.getText()))
                .build();

        tablist.getPlayer().sendPacket(packet);

        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(Tablist tablist, TabEntry tabEntry, Skin skin) {
        if (tabEntry.getTexture().equals(skin)){
            return;
        }

        final MCVersion version = tablist.getPlayer().getVersion();
        if (version == MCVersion.V1_7) {
            return;
        }

        MCGameProfile gameProfile = this.getGameProfile(version, tabEntry);

        gameProfile.clearProperties();
        gameProfile.setProperty(new Property("textures", skin.skinValue, skin.skinSignature));

        final PlayerInfoData playerInfoData = new PlayerInfoData(tabEntry.getLatency(), gameProfile, GameMode.SURVIVAL, tabEntry.getText());
        final PacketPlay.Out.PlayerInfo remove = PacketPlay.Out.PlayerInfo.builder()
                .action(PlayerInfoAction.REMOVE_PLAYER)
                .entries(playerInfoData)
                .build();
        final PacketPlay.Out.PlayerInfo add = PacketPlay.Out.PlayerInfo.builder()
                .action(PlayerInfoAction.ADD_PLAYER)
                .entries(playerInfoData)
                .build();

        tablist.getPlayer().sendPacket(remove);
        tablist.getPlayer().sendPacket(add);

        tabEntry.setTexture(skin);
    }

    @Override
    public void updateHeaderAndFooter(Tablist tablist, Component header, Component footer) {
        MCPlayer player = tablist.getPlayer();
        if (player.getVersion() == MCVersion.V1_7) {
            return;
        }

        final PacketPlay.Out.Tablist packet = PacketPlay.Out.Tablist.builder()
                .header(header)
                .footer(footer)
                .build();
        player.sendPacket(packet);
    }

    private MCGameProfile getGameProfile(MCVersion version, TabEntry tabEntry) {
        return MCGameProfile.create(tabEntry.getUuid(), version != MCVersion.V1_7 ? tabEntry.getId() : LegacyClientUtil.entry(tabEntry.getRawSlot() - 1) + "");
    }
}
