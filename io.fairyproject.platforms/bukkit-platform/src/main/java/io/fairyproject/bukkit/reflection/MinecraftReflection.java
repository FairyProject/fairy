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

package io.fairyproject.bukkit.reflection;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.impl.annotation.ProviderTestImpl;
import io.fairyproject.bukkit.impl.test.ImplementationFactory;
import io.fairyproject.bukkit.reflection.annotation.ProtocolImpl;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.bukkit.reflection.resolver.ConstructorResolver;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import io.fairyproject.bukkit.reflection.version.protocol.ProtocolCheck;
import io.fairyproject.bukkit.reflection.wrapper.ChatComponentWrapper;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.bukkit.reflection.wrapper.PacketWrapper;
import io.fairyproject.log.Log;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.EquivalentConverter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Helper class to access minecraft/bukkit specific objects
 */
public class MinecraftReflection {

    public static String NETTY_PREFIX;

    private static final NMSClassResolver NMS_CLASS_RESOLVER = new NMSClassResolver();
    private static final OBCClassResolver OBC_CLASS_RESOLVER = new OBCClassResolver();
    private static Class<?> NMS_ENTITY;
    private static Class<?> CRAFT_ENTITY;

    /**
     * The CraftPlayer.getHandle method
     */
    private static MethodWrapper PLAYER_GET_HANDLE;

    /**
     * Get game profile
     */
    private static MethodWrapper PLAYER_GET_GAME_PROFILE;

    /**
     * The EntityPlayer.playerConnection field
     */
    private static FieldWrapper FIELD_PLAYER_CONNECTION;

    /**
     * The PlayerConnection.networkManager field
     */
    private static FieldWrapper FIELD_NETWORK_MANAGER;

    /**
     * The NetworkManager.channel field
     */
    private static FieldWrapper FIELD_CHANNEL;

    /**
     * Netty Channel Type
     */
    public static Class<?> CHANNEL_TYPE;

    /**
     * GameProfile Type
     */
    public static Class<?> GAME_PROFILE_TYPE;

    /**
     * The PlayerConnection.sendPacket method
     */
    private static MethodWrapper<Void> METHOD_SEND_PACKET;

    private static ProtocolCheck PROTOCOL_CHECK;

    private static Function<Integer, Integer> ENTITY_ID_RESOLVER;

    private static FieldWrapper<Integer> PING_FIELD;

    public static MCVersion getProtocol(Player player) {
        return MCVersion.getVersionFromRaw(PROTOCOL_CHECK.getVersion(player));
    }

    public static void init() {
        if (Debug.UNIT_TEST) {
            return;
        }

        try {
            NMS_ENTITY = NMS_CLASS_RESOLVER.resolve("world.entity.Entity", "Entity");
            CRAFT_ENTITY = OBC_CLASS_RESOLVER.resolve("entity.CraftEntity");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        try {
            CHANNEL_TYPE = Class.forName("io.netty.channel.Channel");
            NETTY_PREFIX = "io.netty.";
        } catch (ClassNotFoundException ex) {

            try {
                CHANNEL_TYPE = Class.forName("net.minecraft.util.io.netty.channel.Channel");
                NETTY_PREFIX = "net.minecraft.util.io.netty.";
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Coulnd't find netty Channel class!", ex2);
            }

        }

        try {
            GAME_PROFILE_TYPE = Class.forName("com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException ex) {
            try {
                GAME_PROFILE_TYPE = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Coulnd't find mojang GameProfile class!", ex2);
            }
        }

        try {
            Class<?> entityPlayerType = NMS_CLASS_RESOLVER.resolve("server.level.EntityPlayer", "EntityPlayer");
            Class<?> playerConnectionType = NMS_CLASS_RESOLVER.resolve("server.network.PlayerConnection", "PlayerConnection");
            Class<?> networkManagerType = NMS_CLASS_RESOLVER.resolve("network.NetworkManager", "NetworkManager");

            Class<?> craftPlayerType = OBC_CLASS_RESOLVER.resolve("entity.CraftPlayer");

            MinecraftReflection.PLAYER_GET_HANDLE = new MethodWrapper(craftPlayerType.getDeclaredMethod("getHandle"));
            MinecraftReflection.PLAYER_GET_GAME_PROFILE = new MethodWrapper(craftPlayerType.getDeclaredMethod("getProfile"));
            MinecraftReflection.FIELD_PLAYER_CONNECTION = new FieldResolver(entityPlayerType)
                    .resolveByFirstTypeDynamic(playerConnectionType);

            Class<?> packetClass = NMS_CLASS_RESOLVER.resolve("network.protocol.Packet", "Packet");

            Method sendPacket;
            try {
                sendPacket = playerConnectionType.getDeclaredMethod("sendPacket", packetClass);
            } catch (NoSuchMethodException ex) {
                sendPacket = playerConnectionType.getDeclaredMethod("a", packetClass);
            }
            MinecraftReflection.METHOD_SEND_PACKET = new MethodWrapper<>(sendPacket);

            MinecraftReflection.FIELD_NETWORK_MANAGER = new FieldResolver(playerConnectionType)
                    .resolveByFirstTypeWrapper(networkManagerType);

            MinecraftReflection.FIELD_CHANNEL = new FieldResolver(networkManagerType)
                    .resolveByFirstTypeDynamic(CHANNEL_TYPE);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Something went wrong when doing reflection", throwable);
        }

        MinecraftReflection.initProtocolCheck();
    }

    private static void initProtocolCheck() {
        ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .overrideClassLoaders(MinecraftReflection.class.getClassLoader())
                .acceptPackages(Fairy.getFairyPackage())
                .scan();

        Class<?> lastSuccess = null;
        lookup:
        for (Class<?> type : scanResult.getClassesWithAnnotation(ProtocolImpl.class).loadClasses()) {
            if (!ProtocolCheck.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("The type " + type.getName() + " does not implement to ProtocolCheck!");
            }

            ImplementationFactory.TestResult result = ImplementationFactory.test(type.getAnnotation(ProviderTestImpl.class));
            switch (result) {
                case NO_PROVIDER:
                    lastSuccess = type;
                    break;
                case SUCCESS:
                    lastSuccess = type;
                    break lookup;
                case FAILURE:
                    break;
            }
        }

        if (lastSuccess == null) {
            throw new UnsupportedOperationException("Couldn't find any usable protocol check! (but it shouldn't be possible)");
        }

        try {
            PROTOCOL_CHECK = (ProtocolCheck) lastSuccess.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        Log.info("Initialized Protocol Check with " + lastSuccess.getSimpleName());
    }

    /**
     * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
     */
    public static String getVersion() {
        return OBCVersion.get().packageName() + ".";
    }

    /**
     * @return the current NMS version package
     */
    public static String getNMSPackage() {
        return OBCVersion.get().getNmsPackage();
    }

    /**
     * @return the current OBC package
     */
    public static String getOBCPackage() {
        return OBCVersion.get().getObcPackage();
    }

    public static <T> T getChannel(Player player) {
        Object entityPlayer = MinecraftReflection.PLAYER_GET_HANDLE.invoke(player);
        Object playerConnection = MinecraftReflection.FIELD_PLAYER_CONNECTION.get(entityPlayer);
        Object networkManager = MinecraftReflection.FIELD_NETWORK_MANAGER.get(playerConnection);
        return (T) MinecraftReflection.FIELD_CHANNEL.get(networkManager);
    }

    public static Object getGameProfile(Player player) {
        return PLAYER_GET_GAME_PROFILE.invoke(player);
    }

    public static void sendPacket(Player player, Object packet) {
        Object entityPlayer = MinecraftReflection.PLAYER_GET_HANDLE.invoke(player);
        Object playerConnection = MinecraftReflection.FIELD_PLAYER_CONNECTION.get(entityPlayer);
        MinecraftReflection.METHOD_SEND_PACKET.invoke(playerConnection, packet);
    }

    public static int getNewEntityId() {
        return MinecraftReflection.setEntityId(1);
    }

    public static int setEntityId(int newIds) {
        if (ENTITY_ID_RESOLVER == null) {
            Class<?> entityClass = NMS_CLASS_RESOLVER.resolveSilent("world.entity.Entity", "Entity");
            try {
                FieldWrapper fieldWrapper = new FieldResolver(entityClass).resolveWrapper("entityCount");
                ENTITY_ID_RESOLVER = n -> {
                    int id = (int) fieldWrapper.get(null);
                    fieldWrapper.set(null, id + n);
                    return id; // it's id++ so we return the old one
                };
            } catch (Throwable throwable) {
                FieldWrapper fieldWrapper = new FieldResolver(entityClass).resolveWrapper("ENTITY_COUNTER");
                AtomicInteger id = (AtomicInteger) fieldWrapper.get(null);

                ENTITY_ID_RESOLVER = id::addAndGet;
            }
        }

        return ENTITY_ID_RESOLVER.apply(newIds);
    }

    public static void sendPacket(Player player, PacketWrapper packetWrapper) {
        MinecraftReflection.sendPacket(player, packetWrapper.getPacket());
    }

    public static Object getHandle(Object object) throws ReflectiveOperationException {
        Method method;
        try {
            method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getHandle"));
        } catch (ReflectiveOperationException e) {
            method = AccessUtil.setAccessible(CRAFT_ENTITY.getDeclaredMethod("getHandle"));
        }
        return method.invoke(object);
    }

    public static Entity getBukkitEntity(Object object) throws ReflectiveOperationException {
        Method method;
        try {
            method = AccessUtil.setAccessible(NMS_ENTITY.getDeclaredMethod("getBukkitEntity"));
        } catch (ReflectiveOperationException e) {
            method = AccessUtil.setAccessible(CRAFT_ENTITY.getDeclaredMethod("getHandle"));
        }
        return (Entity) method.invoke(object);
    }

    public static Object getHandleSilent(Object object) {
        try {
            return getHandle(object);
        } catch (Exception e) {
        }
        return null;
    }

    public static int getPing(Player player) {
        if (PING_FIELD == null) {
            try {
                Class<?> type = NMS_CLASS_RESOLVER.resolve("server.level.EntityPlayer", "EntityPlayer");
                PING_FIELD = new FieldResolver(type).resolveWrapper("ping");
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        Object nmsPlayer = PLAYER_GET_HANDLE.invoke(player);
        return PING_FIELD.get(nmsPlayer);
    }

    @Deprecated
    public static Object newEnumInstance(Class clazz, Class[] types, Object[] values) throws ReflectiveOperationException {
        Constructor constructor = new ConstructorResolver(clazz).resolve(types);
        Field accessorField = new FieldResolver(Constructor.class).resolve("constructorAccessor");
        Object constructorAccessor = accessorField.get(constructor);
        if (constructorAccessor == null) {
            new MethodResolver(Constructor.class).resolve("acquireConstructorAccessor").invoke(constructor);
            constructorAccessor = accessorField.get(constructor);
        }
        return new MethodResolver(constructorAccessor.getClass()).resolve("newInstance").invoke(constructorAccessor, (Object) values);
    }


    public static Class<? extends Enum> getEnumGamemodeClass() {
        try {
            return NMS_CLASS_RESOLVER.resolve("world.level.EnumGamemode", "EnumGamemode");
        } catch (Throwable throwable) {
            try {
                Class<? extends Enum> type = NMS_CLASS_RESOLVER.resolve("world.level.WorldSettings$EnumGamemode", "WorldSettings$EnumGamemode");
                NMS_CLASS_RESOLVER.cache("EnumGamemode", type);
                return type;
            } catch (Throwable throwable1) {
                throw new RuntimeException(throwable1);
            }
        }
    }

    public static Class<?> getIChatBaseComponentClass() {
        try {
            return NMS_CLASS_RESOLVER.resolve("network.chat.IChatBaseComponent","IChatBaseComponent");
        } catch (ClassNotFoundException ex) {
            try {
                return OBC_CLASS_RESOLVER
                        .resolve("util.CraftChatMessage")
                        .getMethod("fromString", String.class)
                        .getReturnType().getComponentType();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    public static Class<?> getChatModifierClass() {
        try {
            return NMS_CLASS_RESOLVER.resolve("network.chat.ChatModifier","ChatModifier");
        } catch (Throwable throwable) {
            try {
                return NMS_CLASS_RESOLVER.resolveSubClass(getIChatBaseComponentClass(), "ChatModifier");
            } catch (Throwable throwable1) {
                throw new RuntimeException(throwable1);
            }
        }
    }

    public static Class<? extends Enum> getEnumChatFormatClass() {
        try {
            return NMS_CLASS_RESOLVER.resolve("EnumChatFormat");
        } catch (Throwable throwable) {
            try {
                Class<? extends Enum> type = NMS_CLASS_RESOLVER.resolveSubClass(getIChatBaseComponentClass(), "EnumChatFormat");
                NMS_CLASS_RESOLVER.cache("ChatModifier", type);
                return type;
            } catch (Throwable throwable1) {
                throw new RuntimeException(throwable1);
            }
        }
    }

    private static EquivalentConverter.EnumConverter<GameMode> GAME_MODE_CONVERTER;
    private static EquivalentConverter.EnumConverter<ChatColor> CHAT_COLOR_CONVERTER;

    private static EquivalentConverter<ChatComponentWrapper> CHAT_COMPONENT_CONVERTER;

    public static Class<? extends Enum> getHealthDisplayTypeClass() {
        try {
            return NMS_CLASS_RESOLVER.resolve("EnumScoreboardHealthDisplay");
        } catch (Throwable throwable) {
            try {
                Class<? extends Enum> type = NMS_CLASS_RESOLVER.resolve("IScoreboardCriteria$EnumScoreboardHealthDisplay");
                NMS_CLASS_RESOLVER.cache("EnumScoreboardHealthDisplay", type);
                return type;
            } catch (Throwable throwable1) {
                throw new RuntimeException(throwable1);
            }
        }
    }

    public static EquivalentConverter.EnumConverter<GameMode> getGameModeConverter() {
        if (GAME_MODE_CONVERTER == null) {
            GAME_MODE_CONVERTER = new EquivalentConverter.EnumConverter<GameMode>(getEnumGamemodeClass(), GameMode.class) {
                @Nullable
                @Override
                public Object getDefaultGeneric() {
                    try {
                        return Enum.valueOf(this.getGenericType(), "NOT_SET");
                    } catch (IllegalArgumentException ex) { // 1.7
                        return Enum.valueOf(this.getGenericType(), "NONE");
                    }
                }
            };
        }

        return GAME_MODE_CONVERTER;
    }

    public static EquivalentConverter.EnumConverter<ChatColor> getChatColorConverter() {
        if (CHAT_COLOR_CONVERTER == null) {
            Class<? extends Enum> enumChatFormat = getEnumChatFormatClass();

            CHAT_COLOR_CONVERTER = new EquivalentConverter.EnumConverter<>(enumChatFormat, ChatColor.class);
        }

        return CHAT_COLOR_CONVERTER;
    }

    public static EquivalentConverter<ChatComponentWrapper> getChatComponentConverter() {
        if (CHAT_COMPONENT_CONVERTER != null) {
            return CHAT_COMPONENT_CONVERTER;
        }

        return CHAT_COMPONENT_CONVERTER = handle(ChatComponentWrapper::getHandle, ChatComponentWrapper::fromHandle);
    }

    public static <T> EquivalentConverter<T> handle(final Function<T, Object> toHandle,
                                                    final Function<Object, T> fromHandle) {
        return new EquivalentConverter<T>() {
            @Override
            public T getSpecific(Object generic) {
                return fromHandle.apply(generic);
            }

            @Override
            public Object getGeneric(T specific) {
                return toHandle.apply(specific);
            }

            @Override
            public Class<T> getSpecificType() {
                return null;
            }
        };
    }
}
