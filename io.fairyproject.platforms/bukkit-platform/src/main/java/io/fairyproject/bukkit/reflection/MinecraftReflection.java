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

import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.impl.annotation.ProviderTestImpl;
import io.fairyproject.bukkit.impl.test.ImplementationFactory;
import io.fairyproject.bukkit.reflection.annotation.ProtocolImpl;
import io.fairyproject.bukkit.reflection.minecraft.MinecraftVersion;
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
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.reflect.ReflectLookup;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.EquivalentConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to access minecraft/bukkit specific objects
 */
public class MinecraftReflection {
    public static final Pattern NUMERIC_VERSION_PATTERN = Pattern.compile("v([0-9])_([0-9]*)_R([0-9])");

    public static final MinecraftVersion MINECRAFT_VERSION = MinecraftVersion.VERSION;

    public static String NETTY_PREFIX;

    private static NMSClassResolver NMS_CLASS_RESOLVER = new NMSClassResolver();
    private static OBCClassResolver OBC_CLASS_RESOLVER = new OBCClassResolver();
    private static Class<?> NMS_ENTITY;
    private static Class<?> CRAFT_ENTITY;

    /**
     * The CraftPlayer.getHandle method
     */
    private static MethodWrapper<?> PLAYER_GET_HANDLE;

    /**
     * Get game profile
     */
    private static MethodWrapper<?> PLAYER_GET_GAME_PROFILE;

    /**
     * The EntityPlayer.playerConnection field
     */
    private static FieldWrapper<?> FIELD_PLAYER_CONNECTION;

    /**
     * The PlayerConnection.networkManager field
     */
    private static FieldWrapper<?> FIELD_NETWORK_MANAGER;

    /**
     * The NetworkManager.channel field
     */
    private static FieldWrapper<?> FIELD_CHANNEL;

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

    public static MCVersion getProtocol(Player player) {
        return MCVersion.getVersionFromRaw(PROTOCOL_CHECK.getVersion(player));
    }

    public static void init() {
        try {
            Version.runSanityCheck();
        } catch (Exception e) {
            throw new RuntimeException("Sanity check which should always succeed just failed! Am I crazy?!", e);
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
            Class<?> entityHumanType = NMS_CLASS_RESOLVER.resolve("world.entity.player.EntityHuman", "EntityHuman");
            Class<?> entityPlayerType = NMS_CLASS_RESOLVER.resolve("server.level.EntityPlayer", "EntityPlayer");
            Class<?> playerConnectionType = NMS_CLASS_RESOLVER.resolve("server.network.PlayerConnection", "PlayerConnection");
            Class<?> networkManagerType = NMS_CLASS_RESOLVER.resolve("network.NetworkManager", "NetworkManager");

            MinecraftReflection.PLAYER_GET_HANDLE = new MethodWrapper<>(OBC_CLASS_RESOLVER.resolve("entity.CraftPlayer").getDeclaredMethod("getHandle"));
            MethodWrapper<?> playerProfileMethod;
            try {
                playerProfileMethod = new MethodWrapper<>(entityHumanType.getMethod("getProfile"));
            } catch (Exception ex) {
                playerProfileMethod = new MethodWrapper<>(entityHumanType.getMethod("fp"));
            }
            MinecraftReflection.PLAYER_GET_GAME_PROFILE = playerProfileMethod;

            MinecraftReflection.FIELD_PLAYER_CONNECTION = new FieldResolver(entityPlayerType)
                    .resolveByFirstTypeDynamic(playerConnectionType);

            Class<?> packetClass = NMS_CLASS_RESOLVER.resolve("network.protocol.Packet", "Packet");

            MethodWrapper<Void> sendPacketMethod;
            try {
                sendPacketMethod = new MethodWrapper<>(playerConnectionType.getDeclaredMethod("sendPacket", packetClass));
            } catch (Exception ex) {
                sendPacketMethod = new MethodWrapper<>(playerConnectionType.getDeclaredMethod("a", packetClass));
            }
            MinecraftReflection.METHOD_SEND_PACKET = sendPacketMethod;

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
        ReflectLookup reflectLookup = new ReflectLookup(Collections.singletonList(MinecraftReflection.class.getClassLoader()), Collections.singletonList("io.fairyproject"));

        Class<?> lastSuccess = null;
        lookup:
        for (Class<?> type : reflectLookup.findAnnotatedClasses(ProtocolImpl.class)) {
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
        Imanity.LOGGER.info("Initialized Protocol Check with " + lastSuccess.getSimpleName());
    }

    /**
     * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
     */
    public static String getVersion() {
        return MinecraftVersion.VERSION.packageName() + ".";
    }

    /**
     * @return the current NMS version package
     */
    public static String getNMSPackage() {
        return MinecraftVersion.VERSION.getNmsPackage();
    }

    /**
     * @return the current OBC package
     */
    public static String getOBCPackage() {
        return MinecraftVersion.VERSION.getObcPackage();
    }

    public static <T> T getChannel(Player player) {
        Object entityPlayer = MinecraftReflection.PLAYER_GET_HANDLE.invoke(player);
        Object playerConnection = MinecraftReflection.FIELD_PLAYER_CONNECTION.get(entityPlayer);
        Object networkManager = MinecraftReflection.FIELD_NETWORK_MANAGER.get(playerConnection);
        return (T) MinecraftReflection.FIELD_CHANNEL.get(networkManager);
    }

    public static Object getGameProfile(Player player) {
        Object entityPlayer = MinecraftReflection.PLAYER_GET_HANDLE.invoke(player);
        return PLAYER_GET_GAME_PROFILE.invoke(entityPlayer);
    }

    public static void sendPacket(Player player, Object packet) {
        Object entityPlayer = MinecraftReflection.PLAYER_GET_HANDLE.invoke(player);
        Object playerConnection = MinecraftReflection.FIELD_PLAYER_CONNECTION.get(entityPlayer);
        MinecraftReflection.METHOD_SEND_PACKET.invoke(playerConnection, packet);
    }

    private static FieldWrapper<Integer> ENTITY_ID_RESOLVER;

    public static int getNewEntityId() {
        return MinecraftReflection.setEntityId(1);
    }

    public static int setEntityId(int newIds) {
        if (ENTITY_ID_RESOLVER == null) {
            ENTITY_ID_RESOLVER = new FieldResolver(NMS_CLASS_RESOLVER.resolveSilent("world.entity.Entity", "Entity"))
                    .resolveWrapper("entityCount");
        }

        int id = ENTITY_ID_RESOLVER.get(null);
        ENTITY_ID_RESOLVER.setSilent(null, id + newIds);
        return id;
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

    private static FieldWrapper<Integer> PING_FIELD;

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

    public enum Version {
        UNKNOWN(-1) {
            @Override
            public boolean matchesPackageName(String packageName) {
                return false;
            }
        },

        v1_7_R1(10701),
        v1_7_R2(10702),
        v1_7_R3(10703),
        v1_7_R4(10704),

        v1_8_R1(10801),
        v1_8_R2(10802),
        v1_8_R3(10803),
        //Does this even exists?
        v1_8_R4(10804),

        v1_9_R1(10901),
        v1_9_R2(10902),

        v1_10_R1(11001),

        v1_11_R1(11101),

        v1_12_R1(11201),

        v1_13_R1(11301),
        v1_13_R2(11302),

        v1_14_R1(11401),

        v1_15_R1(11501),

        v1_16_R1(11601),
        v1_16_R2(11602),
        v1_16_R3(11603),

        v1_17_R1(11701),

        v1_18_R1(11801),

        /// (Potentially) Upcoming versions
        v1_19_R1(11901),

        v1_20_R1(12001);

        private final MinecraftVersion version;

        Version(int version, String nmsFormat, String obcFormat, boolean nmsVersionPrefix) {
            this.version = new MinecraftVersion(name(), version, nmsFormat, obcFormat, nmsVersionPrefix);
        }

        Version(int version) {
            if (version >= 11701) { // 1.17+ new class package name format
                this.version = new MinecraftVersion(name(), version, "net.minecraft", "org.bukkit.craftbukkit.%s", false);
            } else {
                this.version = new MinecraftVersion(name(), version);
            }
        }

        /**
         * @return the version-number
         */
        public int version() {
            return version.version();
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is older than the specified version
         */
        @Deprecated
        public boolean olderThan(Version version) {
            return version() < version.version();
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is newer than the specified version
         */
        @Deprecated
        public boolean newerThan(Version version) {
            return version() >= version.version();
        }

        /**
         * @param oldVersion The older version to check
         * @param newVersion The newer version to check
         * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
         */
        @Deprecated
        public boolean inRange(Version oldVersion, Version newVersion) {
            return newerThan(oldVersion) && olderThan(newVersion);
        }

        public boolean matchesPackageName(String packageName) {
            return packageName.toLowerCase().contains(name().toLowerCase());
        }

        /**
         * @return the minecraft version
         */
        public MinecraftVersion minecraft() {
            return version;
        }

        static void runSanityCheck() {
            assert v1_14_R1.newerThan(v1_13_R2);
            assert v1_13_R2.olderThan(v1_14_R1);

            assert v1_13_R2.newerThan(v1_8_R1);

            assert v1_13_R2.newerThan(v1_8_R1) && v1_13_R2.olderThan(v1_14_R1);
        }

        @Override
        public String toString() {
            return name() + " (" + version() + ")";
        }
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
