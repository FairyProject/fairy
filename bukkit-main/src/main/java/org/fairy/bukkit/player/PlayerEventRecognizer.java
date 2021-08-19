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

package org.fairy.bukkit.player;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerEvent;
import org.fairy.bukkit.events.player.IPlayerEvent;
import org.fairy.reflect.Reflect;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@UtilityClass
public class PlayerEventRecognizer {

    private static final Map<Class<? extends Attribute<?>>, Attribute<?>> ATTRIBUTE_INSTANCE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Function<Event, Player>> EVENT_PLAYER_METHODS = new ConcurrentHashMap<>();
    private static final Set<Class<?>> NO_METHODS = Sets.newConcurrentHashSet();

    static {

        EVENT_PLAYER_METHODS.put(BlockBreakEvent.class, event -> ((BlockBreakEvent) event).getPlayer());
        EVENT_PLAYER_METHODS.put(BlockPlaceEvent.class, event -> ((BlockPlaceEvent) event).getPlayer());
        EVENT_PLAYER_METHODS.put(FoodLevelChangeEvent.class, event -> (Player) ((FoodLevelChangeEvent) event).getEntity());

    }

    public boolean isTypePossible(Class<? extends Event> type) {
        if (PlayerEvent.class.isAssignableFrom(type)) {
            return true;
        }

        if (IPlayerEvent.class.isAssignableFrom(type)) {
            return true;
        }

        if (EntityEvent.class.isAssignableFrom(type)) {
            return true;
        }

        if (EVENT_PLAYER_METHODS.containsKey(type)) {
            return true;
        }

        return searchMethod(type) != null;
    }

    @SafeVarargs
    @Nullable
    public Player tryRecognize(Event event, Class<? extends Attribute<?>>... attributes) {
        Player player = tryRecognize0(event);
        player = transformEvent(event, player, attributes);
        return player;
    }

    private Player tryRecognize0(Event event) {
        if (event instanceof PlayerEvent) {
            return ((PlayerEvent) event).getPlayer();
        }

        if (event instanceof IPlayerEvent) {
            return ((IPlayerEvent) event).getPlayer();
        }

        if (event instanceof EntityEvent) {
            Entity entity = ((EntityEvent) event).getEntity();
            if (entity instanceof Player) {
                return (Player) entity;
            }
            return null;
        }

        final Class<? extends Event> type = event.getClass();

        if (EVENT_PLAYER_METHODS.containsKey(type)) {
            return EVENT_PLAYER_METHODS.get(type).apply(event);
        } else {
            final MethodHandleFunction methodHandleFunction = searchMethod(type);

            if (methodHandleFunction != null) {
                return methodHandleFunction.apply(event);
            }
        }

        return null;
    }

    private MethodHandleFunction searchMethod(Class<? extends Event> type) {
        MethodHandle methodHandle;

        if (!NO_METHODS.contains(type)) {
            for (Method method : type.getMethods()) {
                if (method.getParameterCount() == 0) {
                    Class<?> returnType = method.getReturnType();
                    if (Player.class.isAssignableFrom(returnType) || HumanEntity.class.isAssignableFrom(returnType)) {
                        try {
                            methodHandle = Reflect.lookup().unreflect(method);

                            MethodHandleFunction methodHandleFunction = new MethodHandleFunction(methodHandle);
                            EVENT_PLAYER_METHODS.put(type, methodHandleFunction);

                            return methodHandleFunction;
                        } catch (Throwable throwable) {
                            throw new IllegalArgumentException("Something wrong while looking for player", throwable);
                        }
                    }
                }
            }
            NO_METHODS.add(type);
        }

        return null;
    }

    @SafeVarargs
    private Player transformEvent(Event event, Player player, Class<? extends Attribute<?>>... attributes) {
        for (Class<? extends Attribute<?>> type : attributes) {
            final Attribute<?> attribute = getAttributeInstance(type);
            if (attribute.type().isInstance(event)) {
                player = attribute.transform0(event, player);
            }
        }

        return player;
    }

    private Attribute<?> getAttributeInstance(Class<? extends Attribute<?>> type) {
        return ATTRIBUTE_INSTANCE.computeIfAbsent(type, ignored -> {
            try {
                return type.newInstance();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }

    public interface Attribute<T extends Event> {

        Class<T> type();

        default Player transform0(Event event, Player player) {
            return this.transform(this.type().cast(event), player);
        }

        Player transform(T t, @Nullable Player player);

    }

    @RequiredArgsConstructor
    private static class MethodHandleFunction implements Function<Event, Player> {

        private final MethodHandle methodHandle;

        @Override
        public Player apply(Event event) {
            try {
                Object entity = methodHandle.invoke(event);
                if (entity instanceof HumanEntity) {
                    return (Player) entity;
                }
            } catch (Throwable throwable) {
                throw new IllegalArgumentException("Something wrong while looking for player", throwable);
            }
            return null;
        }
    }
}
