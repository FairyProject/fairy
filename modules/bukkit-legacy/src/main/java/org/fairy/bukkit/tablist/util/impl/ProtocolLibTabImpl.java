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

package org.fairy.bukkit.tablist.util.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.fairy.bukkit.FairyBukkitPlatform;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.mc.protocol.MCVersion;
import org.fairy.bukkit.tablist.ImanityTablist;
import org.fairy.bukkit.tablist.util.IImanityTabImpl;
import org.fairy.bukkit.tablist.util.LegacyClientUtil;
import org.fairy.bukkit.tablist.util.TabColumn;
import org.fairy.bukkit.tablist.util.TabEntry;
import org.fairy.bukkit.util.Skin;
import org.fairy.bukkit.Imanity;
import org.fairy.util.CC;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.UUID;

public class ProtocolLibTabImpl implements IImanityTabImpl {

    public ProtocolLibTabImpl() {
    }

    @Override
    public void registerLoginListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(FairyBukkitPlatform.PLUGIN, PacketType.Play.Server.LOGIN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.getPacket().getIntegers().write(2, 60);
            }
        });
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist tablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        UUID uuid = UUID.randomUUID();
        final Player player = tablist.getPlayer();
        final MCVersion MCVersion = MinecraftReflection.getProtocol(player);

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        WrappedGameProfile profile = new WrappedGameProfile(uuid, MCVersion != MCVersion.v1_7  ? string : LegacyClientUtil.entry(rawSlot - 1) + "");
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(MCVersion != MCVersion.v1_7 ?  "" : profile.getName()));
        if (MCVersion != MCVersion.v1_7) {
            playerInfoData.getProfile().getProperties().put("texture", new WrappedSignedProperty("textures", Skin.GRAY.skinValue, Skin.GRAY.skinSignature));
        }
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        sendPacket(player, packet);
        return new TabEntry(string, uuid, "", tablist, Skin.GRAY, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(ImanityTablist tablist, TabEntry tabEntry, String text) {
        if (tabEntry.getText().equals(text)) {
            return;
        }

        final Player player = tablist.getPlayer();
        final MCVersion MCVersion = MinecraftReflection.getProtocol(player);
        if (MCVersion == MCVersion.v1_7) {
            String[] newStrings = ImanityTablist.splitStrings(text, tabEntry.getRawSlot());
            Imanity.IMPLEMENTATION.sendTeam(
                    player,
                    LegacyClientUtil.name(tabEntry.getRawSlot() - 1),
                    CC.translate(newStrings[0]),
                    newStrings.length > 1 ? CC.translate(newStrings[1]) : "",
                    Collections.singleton(LegacyClientUtil.entry(tabEntry.getRawSlot() - 1)),
                    2
            );
        }else {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME);
            WrappedGameProfile profile = new WrappedGameProfile(
                    tabEntry.getUuid(),
                    tabEntry.getId()
            );
            PlayerInfoData playerInfoData = new PlayerInfoData(
                    profile,
                    1,
                    EnumWrappers.NativeGameMode.SURVIVAL,
                    WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', text))
            );
            packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            sendPacket(player, packet);
        }
        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist tablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) {
            return;
        }

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_LATENCY);

        WrappedGameProfile profile = new WrappedGameProfile(
                tabEntry.getUuid(),
                tabEntry.getId()
        );

        PlayerInfoData playerInfoData = new PlayerInfoData(
                profile,
                latency,
                EnumWrappers.NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', tabEntry.getText()))
        );

        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        sendPacket(tablist.getPlayer(), packet);
        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist zigguratTablist, TabEntry tabEntry, Skin skin) {
        final Player player = zigguratTablist.getPlayer();
        final MCVersion MCVersion = MinecraftReflection.getProtocol(player);
        if (tabEntry.getTexture() == skin || MCVersion == MCVersion.v1_7) {
            return;
        }

        WrappedGameProfile profile = new WrappedGameProfile(tabEntry.getUuid(), tabEntry.getId());
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(CC.translate(tabEntry.getText())));

        playerInfoData.getProfile().getProperties().put("texture", new WrappedSignedProperty("textures", skin.skinValue, skin.skinSignature));

        PacketContainer remove = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        remove.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        remove.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));


        PacketContainer add = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        add.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        add.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        sendPacket(player, remove);
        sendPacket(player, add);

        tabEntry.setTexture(skin);
    }

    @Override
    public void updateHeaderAndFooter(ImanityTablist tablist, String header, String footer) {
        PacketContainer headerAndFooter = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        final Player player = tablist.getPlayer();
        final MCVersion MCVersion = MinecraftReflection.getProtocol(player);

        if (MCVersion != MCVersion.v1_7) {

            headerAndFooter.getChatComponents().write(0, WrappedChatComponent.fromText(header));
            headerAndFooter.getChatComponents().write(1, WrappedChatComponent.fromText(footer));

            sendPacket(player, headerAndFooter);
        }
    }

    private static void sendPacket(Player player, PacketContainer packetContainer){
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
