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

package io.fairyproject.bukkit.packet.wrapper.server.playerinfo;

import lombok.Getter;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.util.EquivalentConverter;

public enum PlayerInfoAction {

    ADD_PLAYER(0),
    UPDATE_GAME_MODE(1),
    UPDATE_LATENCY(2),
    UPDATE_DISPLAY_NAME(3),
    REMOVE_PLAYER(4);

    @Getter
    private final int id;

    PlayerInfoAction(int id) {
        this.id = id;
    }

    public static PlayerInfoAction getById(int id) {
        for (PlayerInfoAction action : PlayerInfoAction.values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        return null;
    }

    private static EquivalentConverter.EnumConverter<PlayerInfoAction> CONVERTER;

    public static EquivalentConverter.EnumConverter<PlayerInfoAction> getConverter() {
        if (CONVERTER != null) {
            return CONVERTER;
        }

        return CONVERTER = new EquivalentConverter.EnumConverter<>(getGenericType(), PlayerInfoAction.class);
    }

    public static Class<? extends Enum> getGenericType() {
        NMSClassResolver classResolver = new NMSClassResolver();
        Class<?> type = classResolver.resolveSilent("PlayerInfoAction");

        if (type == null) {
            type = classResolver.resolveSilent("EnumPlayerInfoAction");

            if (type == null) {
                try {
                    Class<?> mainClass = classResolver.resolve("PacketPlayOutPlayerInfo");
                    type = classResolver.resolveSubClass(mainClass, "EnumPlayerInfoAction");
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }

            classResolver.cache("PlayerInfoAction", type);
        }

        return (Class<? extends Enum>) type;
    }

}