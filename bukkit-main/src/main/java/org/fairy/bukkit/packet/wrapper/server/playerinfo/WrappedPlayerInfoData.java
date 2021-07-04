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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.resolver.ConstructorResolver;
import org.fairy.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.fairy.bukkit.reflection.wrapper.ConstructorWrapper;
import org.fairy.bukkit.reflection.wrapper.GameProfileWrapper;
import org.fairy.bukkit.reflection.wrapper.ObjectWrapper;
import org.fairy.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.fairy.util.EquivalentConverter;

import javax.annotation.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class WrappedPlayerInfoData {

    private static Class<?> PLAYER_INFO_DATA_CLASS;

    public static boolean isPlayerInfoDataExists() {
        return PLAYER_INFO_DATA_CLASS != null;
    }

    private static ConstructorWrapper<?> CONSTRUCTOR;
    private static EquivalentConverter<WrappedPlayerInfoData> CONVERTER;

    static {
        NMSClassResolver classResolver = new NMSClassResolver();
        try {
            Class<?> playerInfoPacketClass = classResolver.resolve("PacketPlayOutPlayerInfo");

            try {
                PLAYER_INFO_DATA_CLASS = classResolver.resolve("PlayerInfoData");
            } catch (ClassNotFoundException ex) {
                try {
                    PLAYER_INFO_DATA_CLASS = classResolver.resolveSubClass(playerInfoPacketClass, "PlayerInfoData");
                } catch (ClassNotFoundException ex2) {
                    // 1.7
                }
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private int latency;

    @Nullable
    private GameMode gameMode;

    private GameProfileWrapper gameProfile;
    private ChatComponentWrapper chatComponent;

    public static WrappedPlayerInfoData from(Player player) {

        GameMode gameMode = player.getGameMode();
        GameProfileWrapper gameProfile = GameProfileWrapper.fromPlayer(player);
        int ping = MinecraftReflection.getPing(player);
        ChatComponentWrapper chatComponent = ChatComponentWrapper.fromText(player.getPlayerListName());

        return new WrappedPlayerInfoData(ping, gameMode, gameProfile, chatComponent);

    }

    public static EquivalentConverter<WrappedPlayerInfoData> getConverter() {
        if (CONVERTER != null) {
            return CONVERTER;
        }
        return CONVERTER = new EquivalentConverter<WrappedPlayerInfoData>() {
            @Override
            public Object getGeneric(WrappedPlayerInfoData specific) {
                if (PLAYER_INFO_DATA_CLASS == null || CONSTRUCTOR == null) {
                    NMSClassResolver classResolver = new NMSClassResolver();

                    try {
                        Class<?> playerInfoPacketClass = classResolver.resolve("PacketPlayOutPlayerInfo");

                        CONSTRUCTOR = new ConstructorResolver(PLAYER_INFO_DATA_CLASS)
                                .resolveMatches(new Class[] {
                                        playerInfoPacketClass,
                                        GameProfileWrapper.IMPLEMENTATION.getGameProfileClass(),
                                        int.class,
                                        MinecraftReflection.getEnumGamemodeClass(),
                                        MinecraftReflection.getIChatBaseComponentClass()
                                },
                                        new Class[] {
                                                GameProfileWrapper.IMPLEMENTATION.getGameProfileClass(),
                                                int.class,
                                                MinecraftReflection.getEnumGamemodeClass(),
                                                MinecraftReflection.getIChatBaseComponentClass()
                                        });
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                }

                try {

                    return CONSTRUCTOR.resolve(
                            new Object[] {
                                    null,
                                    specific.gameProfile.getHandle(),
                                    specific.latency,
                                    MinecraftReflection.getGameModeConverter().getGeneric(specific.getGameMode()),
                                    specific.chatComponent != null ? specific.chatComponent.getHandle() : null
                            },
                            new Object[] {
                                    specific.gameProfile.getHandle(),
                                    specific.latency,
                                    MinecraftReflection.getGameModeConverter().getGeneric(specific.getGameMode()),
                                    specific.chatComponent != null ? specific.chatComponent.getHandle() : null
                            }
                    );

                } catch (Throwable throwable) {
                    throw new RuntimeException("Failed to construct NMS PlayerInfoData", throwable);
                }
            }

            @Override
            public WrappedPlayerInfoData getSpecific(Object generic) {
                ObjectWrapper objectWrapper = new ObjectWrapper(generic);

                GameProfileWrapper gameProfile = (GameProfileWrapper) objectWrapper.getFieldByFirstType(GameProfileWrapper.IMPLEMENTATION.getGameProfileClass());
                int latency = objectWrapper.getFieldByFirstType(int.class);

                GameMode gameMode = MinecraftReflection.getGameModeConverter().getSpecific(objectWrapper.getFieldByFirstType(MinecraftReflection.getEnumGamemodeClass()));

                ChatComponentWrapper chatComponent = MinecraftReflection.getChatComponentConverter().getSpecific(objectWrapper.getFieldByFirstType(ChatComponentWrapper.GENERIC_TYPE));

                return new WrappedPlayerInfoData(latency, gameMode, gameProfile, chatComponent);
            }

            @Override
            public Class<WrappedPlayerInfoData> getSpecificType() {
                return WrappedPlayerInfoData.class;
            }
        };
    }

}
