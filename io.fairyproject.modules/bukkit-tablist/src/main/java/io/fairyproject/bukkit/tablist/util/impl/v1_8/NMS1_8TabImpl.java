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

package io.fairyproject.bukkit.tablist.util.impl.v1_8;

import io.fairyproject.bukkit.packet.wrapper.server.WrappedPacketOutPlayerListHeaderAndFooter;
import io.fairyproject.bukkit.packet.wrapper.server.playerinfo.PlayerInfoAction;
import io.fairyproject.bukkit.packet.wrapper.server.playerinfo.WrappedPacketOutPlayerInfo;
import io.fairyproject.bukkit.packet.wrapper.server.playerinfo.WrappedPlayerInfoData;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.tablist.util.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import io.fairyproject.bukkit.packet.PacketService;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.bukkit.reflection.wrapper.ChatComponentWrapper;
import io.fairyproject.bukkit.reflection.wrapper.GameProfileWrapper;
import io.fairyproject.bukkit.reflection.wrapper.SignedPropertyWrapper;
import io.fairyproject.bukkit.tablist.ImanityTablist;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.util.CC;

import java.util.Collections;
import java.util.UUID;

public class NMS1_8TabImpl implements IImanityTabImpl {

    @Override
    public void removeSelf(Player player) {
        WrappedPacketOutPlayerInfo packet = new WrappedPacketOutPlayerInfo(PlayerInfoAction.REMOVE_PLAYER, player);

        Imanity.getPlayers()
                .stream()
                .filter(online -> MinecraftReflection.getProtocol(online) == MCVersion.V1_7)
                .forEach(online -> PacketService.send(online, packet));
    }

    @Override
    public void registerLoginListener() {

    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist imanityTablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        final Player player = imanityTablist.getPlayer();
        final MCVersion MCVersion = MinecraftReflection.getProtocol(player);

        GameProfileWrapper profile = new GameProfileWrapper(UUID.randomUUID(), MCVersion != MCVersion.V1_7 ? string : LegacyClientUtil.entry(rawSlot - 1) + "");

        if (MCVersion != MCVersion.V1_7) {
            profile.getProperties().put("textures", new SignedPropertyWrapper("textures", Skin.GRAY.skinValue, Skin.GRAY.skinSignature));
        }

        WrappedPacketOutPlayerInfo packet = new WrappedPacketOutPlayerInfo();
        packet.setAction(PlayerInfoAction.ADD_PLAYER);
        packet.getPlayerInfoData().add(new WrappedPlayerInfoData(1, GameMode.SURVIVAL, profile, ChatComponentWrapper.fromText("")));

        PacketService.send(player, packet);

        return new TabEntry(string, profile.getUuid(), "", imanityTablist, Skin.GRAY, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(ImanityTablist imanityTablist, TabEntry tabEntry, String text) {
        if (tabEntry.getText().equals(text)) {
            return;
        }

        final Player player = imanityTablist.getPlayer();
        final MCVersion MCVersion = MinecraftReflection.getProtocol(player);

        String[] newStrings = ImanityTablist.splitStrings(text, tabEntry.getRawSlot());

        if (MCVersion == MCVersion.V1_7) {

            Imanity.IMPLEMENTATION.sendTeam(
                    player,
                    LegacyClientUtil.name(tabEntry.getRawSlot() - 1),
                    CC.translate(newStrings[0]),
                    newStrings.length > 1 ? CC.translate(newStrings[1]) : "",
                    Collections.singleton(LegacyClientUtil.entry(tabEntry.getRawSlot() - 1)),
                    2
            );

        } else {
            ChatComponentWrapper listName = ChatComponentWrapper.fromText(CC.translate(text));

            GameProfileWrapper profile = this.getGameProfile(MCVersion, tabEntry);

            WrappedPacketOutPlayerInfo packet = new WrappedPacketOutPlayerInfo();
            packet.setAction(PlayerInfoAction.UPDATE_DISPLAY_NAME);
            packet.getPlayerInfoData().add(new WrappedPlayerInfoData(tabEntry.getLatency(), GameMode.SURVIVAL, profile, listName));

            PacketService.send(player, packet);
        }

        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist imanityTablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) return;

        ChatComponentWrapper listName = ChatComponentWrapper.fromText(CC.translate(tabEntry.getText()));

        final MCVersion MCVersion = MinecraftReflection.getProtocol(imanityTablist.getPlayer());
        GameProfileWrapper profile = this.getGameProfile(MCVersion, tabEntry);

        WrappedPacketOutPlayerInfo packet = new WrappedPacketOutPlayerInfo();
        packet.setAction(PlayerInfoAction.UPDATE_LATENCY);
        packet.getPlayerInfoData().add(new WrappedPlayerInfoData(latency, GameMode.SURVIVAL, profile, listName));

        PacketService.send(imanityTablist.getPlayer(), packet);

        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist imanityTablist, TabEntry tabEntry, Skin skin) {
        if (tabEntry.getTexture().equals(skin)){
            return;
        }

        final MCVersion MCVersion = MinecraftReflection.getProtocol(imanityTablist.getPlayer());
        if (MCVersion == MCVersion.V1_7) {
            return;
        }

        GameProfileWrapper gameProfile = this.getGameProfile(MCVersion, tabEntry);

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", new SignedPropertyWrapper("textures", skin.skinValue, skin.skinSignature));

        ChatComponentWrapper listName = ChatComponentWrapper.fromText(CC.translate(tabEntry.getText()));

        WrappedPlayerInfoData playerInfoData = new WrappedPlayerInfoData(tabEntry.getLatency(), GameMode.SURVIVAL, gameProfile, listName);

        WrappedPacketOutPlayerInfo packetRemove = new WrappedPacketOutPlayerInfo(PlayerInfoAction.REMOVE_PLAYER, playerInfoData);
        WrappedPacketOutPlayerInfo packetAdd = new WrappedPacketOutPlayerInfo(PlayerInfoAction.ADD_PLAYER, playerInfoData);

        PacketService.send(imanityTablist.getPlayer(), packetRemove);
        PacketService.send(imanityTablist.getPlayer(), packetAdd);

        tabEntry.setTexture(skin);
    }

    @Override
    public void updateHeaderAndFooter(ImanityTablist imanityTablist, String header, String footer) {

        Player player = imanityTablist.getPlayer();
        if (MinecraftReflection.getProtocol(player) == MCVersion.V1_7) {
            return;
        }

        WrappedPacketOutPlayerListHeaderAndFooter packet = new WrappedPacketOutPlayerListHeaderAndFooter(
                ChatComponentWrapper.fromText(header),
                ChatComponentWrapper.fromText(footer)
        );

        PacketService.send(player, packet);

    }

    private GameProfileWrapper getGameProfile(MCVersion MCVersion, TabEntry tabEntry) {
        return new GameProfileWrapper(tabEntry.getUuid(), MCVersion != MCVersion.V1_7 ? tabEntry.getId() : LegacyClientUtil.entry(tabEntry.getRawSlot() - 1) + "");
    }
}
