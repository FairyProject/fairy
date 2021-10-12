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

package org.fairy.bukkit.packet.wrapper.server.playerinfo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.wrapper.PacketContainer;
import org.fairy.bukkit.packet.wrapper.SendableWrapper;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.fairy.bukkit.reflection.wrapper.GameProfileWrapper;
import org.fairy.bukkit.reflection.wrapper.PacketWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@AutowiredWrappedPacket(value = PacketType.Server.PLAYER_INFO, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutPlayerInfo extends WrappedPacket implements SendableWrapper {

    private static Class<?> PACKET_CLASS;

    public static void init() {
        try {
            PACKET_CLASS = NMS_CLASS_RESOLVER.resolve("PacketPlayOutPlayerInfo");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public WrappedPacketOutPlayerInfo() {
        super();
    }

    private PlayerInfoAction action;
    private List<WrappedPlayerInfoData> playerInfoData = new ArrayList<>();

    public WrappedPacketOutPlayerInfo(Object packet) {
        super(packet);
    }

    public WrappedPacketOutPlayerInfo(Player player, Object packet) {
        super(player, packet);
    }

    public WrappedPacketOutPlayerInfo(PlayerInfoAction action, Player player) {
        this.action = action;
        this.playerInfoData.add(WrappedPlayerInfoData.from(player));
    }

    public WrappedPacketOutPlayerInfo(PlayerInfoAction action, WrappedPlayerInfoData... playerInfoData) {
        this.action = action;
        this.playerInfoData.addAll(Arrays.asList(playerInfoData));
    }

    public WrappedPacketOutPlayerInfo(PlayerInfoAction action, Collection<WrappedPlayerInfoData> playerInfoData) {
        this.action = action;
        this.playerInfoData.addAll(playerInfoData);
    }

    @Override
    protected void setup() {

        if (WrappedPlayerInfoData.isPlayerInfoDataExists()) { // 1.8

            action = PlayerInfoAction.getConverter().getSpecific(readObject(0, PlayerInfoAction.getGenericType()));
            for (Object genericInfoData : readObject(0, List.class)) {
                playerInfoData.add(WrappedPlayerInfoData.getConverter().getSpecific(genericInfoData));
            }

        } else { // 1.7

            this.action = PlayerInfoAction.getById(readInt(0));

            GameProfileWrapper gameProfile = this.readGameProfile(0);
            int gamemode = this.readInt(1);
            int ping = this.readInt(2);
            String username = this.readString(0);

            this.playerInfoData.add(new WrappedPlayerInfoData(ping, GameMode.getByValue(gamemode), gameProfile, ChatComponentWrapper.fromText(username)));

        }

    }

    @Override
    public PacketContainer asPacketContainer() {

        if (WrappedPlayerInfoData.isPlayerInfoDataExists()) { // 1.8
            try {
                PacketWrapper packetWrapper = new PacketWrapper(PACKET_CLASS.newInstance());

                packetWrapper.setPacketValueByType(PlayerInfoAction.getGenericType(), PlayerInfoAction.getConverter().getGeneric(this.action));
                List genericInfoDataList = packetWrapper.getPacketValueByIndex(List.class, 0);

                for (WrappedPlayerInfoData playerInfoData : this.playerInfoData) {
                    genericInfoDataList.add(WrappedPlayerInfoData.getConverter().getGeneric(playerInfoData));
                }

                return PacketContainer.of(packetWrapper.getPacket());
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        } else {
            try {

                Object mainPacket = null;
                List<Object> extraPackets = null;

                for (WrappedPlayerInfoData playerInfoData : this.playerInfoData) {
                    PacketWrapper packetWrapper = new PacketWrapper(PACKET_CLASS.newInstance());

                    packetWrapper.setFieldByIndex(int.class, 0, this.action.getId());
                    packetWrapper.setFieldByIndex(GameProfileWrapper.IMPLEMENTATION.getGameProfileClass(), 0, playerInfoData.getGameProfile().getHandle());
                    packetWrapper.setFieldByIndex(int.class, 1, playerInfoData.getGameMode() != null ? playerInfoData.getGameMode().getValue() : -1);
                    packetWrapper.setFieldByIndex(int.class, 2, playerInfoData.getLatency());
                    packetWrapper.setFieldByIndex(String.class, 0, playerInfoData.getChatComponent().toLegacyText());

                    if (mainPacket == null) {
                        mainPacket = packetWrapper.getPacket();
                    } else {
                        if (extraPackets == null) {
                            extraPackets = new ArrayList<>();
                        }

                        extraPackets.add(packetWrapper.getPacket());
                    }
                }

                if (mainPacket == null) {
                    return PacketContainer.empty();
                }

                return PacketContainer.builder()
                        .mainPacket(mainPacket)
                        .extraPackets(extraPackets)
                        .build();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

    }
}
