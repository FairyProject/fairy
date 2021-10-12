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

package org.fairy.bukkit.reflection.wrapper;

import com.google.common.collect.Multimap;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.resolver.FieldResolver;
import org.fairy.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.fairy.bukkit.reflection.wrapper.impl.GameProfileImplementation;

import java.util.UUID;

public class GameProfileWrapper extends WrapperAbstract {

    public static final GameProfileImplementation IMPLEMENTATION = GameProfileImplementation.getImplementation();

    private static FieldWrapper<?> GAME_PROFILE_FIELD;

    static {
        try {
            NMSClassResolver classResolver = new NMSClassResolver();
            Class<?> entityHumanClass = classResolver.resolve("EntityHuman");

            GAME_PROFILE_FIELD = new FieldResolver(entityHumanClass)
                    .resolveByFirstTypeWrapper(GameProfileWrapper.IMPLEMENTATION.getGameProfileClass());
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Getter
    protected Object handle;
    private Multimap<String, SignedPropertyWrapper> propertyMap;

    public GameProfileWrapper(Object handle) {
        this.handle = handle;
    }

    public GameProfileWrapper(UUID uuid, String name) {
        this(IMPLEMENTATION.create(name, uuid));
    }

    public static GameProfileWrapper fromPlayer(Player player) {
        if (GAME_PROFILE_FIELD == null) {
            try {
                NMSClassResolver classResolver = new NMSClassResolver();
                Class<?> entityHumanClass = classResolver.resolve("EntityHuman");

                GAME_PROFILE_FIELD = new FieldResolver(entityHumanClass)
                        .resolveByFirstTypeWrapper(GameProfileWrapper.IMPLEMENTATION.getGameProfileClass());
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        Object nmsPlayer = MinecraftReflection.getHandleSilent(player);
        return new GameProfileWrapper(GAME_PROFILE_FIELD.get(nmsPlayer));
    }

    public String getName() {
        return IMPLEMENTATION.getName(handle);
    }

    public UUID getUuid() {
        return IMPLEMENTATION.getUuid(handle);
    }

    public void setName(String name) {
        IMPLEMENTATION.setName(handle, name);
    }

    public void setUuid(UUID uuid) {
        IMPLEMENTATION.setUuid(handle, uuid);
    }

    public Multimap<String, SignedPropertyWrapper> getProperties() {
        Multimap<String, SignedPropertyWrapper> result = this.propertyMap;

        if (result == null) {
            this.propertyMap = result = IMPLEMENTATION.getProperties(handle);
        }

        return result;
    }

    @Override
    public boolean exists() {
        return handle != null;
    }
}
