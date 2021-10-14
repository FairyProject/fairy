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

package io.fairyproject.bukkit.packet.wrapper.server;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

@AutowiredWrappedPacket(value = PacketType.Server.CHAT, direction = PacketDirection.WRITE)
@Getter
public final class WrappedPacketOutChat extends WrappedPacket implements SendableWrapper {
    private static Constructor<?> chatClassConstructor;
    private static Class<?> packetClass, iChatBaseComponentClass, chatSerializerClass, chatMessageTypeEnum;
    private static MethodWrapper<?> chatMessageTypeCreatorMethod;
    //0 = IChatBaseComponent, Byte
    //1 = IChatBaseComponent, Int
    //2 = IChatBaseComponent, ChatMessageType
    //3 = IChatBaseComponent, ChatMessageType, UUID
    private static byte constructorMode;

    //0 = Byte
    //1 = Byte, Boolean
    private static byte chatTypeMessageEnumConstructorMode;
    private static Map<ChatPosition, Byte> cachedChatPositions;
    private static Map<Byte, ChatPosition> cachedChatPositionIntegers;
    private static Map<String, Byte> cachedChatMessageTypeIntegers;

    private String message;
    private ChatPosition chatPosition;
    private UUID uuid;


    @Deprecated
    public WrappedPacketOutChat(String message) {
        this(message, null);
    }

    public WrappedPacketOutChat(final Object packet) {
        super(packet);
    }

    public WrappedPacketOutChat(String message, UUID uuid) {
        this(message, ChatPosition.CHAT, uuid);
    }

    public WrappedPacketOutChat(String message, ChatPosition chatPosition, UUID uuid) {
        this.uuid = uuid;
        this.message = message;
        this.chatPosition = chatPosition;
    }

    public enum ChatPosition {
        CHAT, SYSTEM_MESSAGE, GAME_INFO
    }


    public static void load() {
        try {
            packetClass = PacketTypeClasses.Server.CHAT;
            iChatBaseComponentClass = NMS_CLASS_RESOLVER.resolve("IChatBaseComponent");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //In 1.8.3+ the ChatSerializer class is declared in the IChatBaseComponent class, so we have to handle that
        try {
            chatSerializerClass = NMS_CLASS_RESOLVER.resolve("ChatSerializer");
        } catch (ClassNotFoundException e) {
            //That is fine, it is probably a subclass
            try {
                chatSerializerClass = NMS_CLASS_RESOLVER.resolveSubClass(iChatBaseComponentClass, "ChatSerializer");
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            }
        }


        boolean isVeryOutdated = false;
        try {
            chatMessageTypeEnum = NMS_CLASS_RESOLVER.resolve("ChatMessageType");
        } catch (ClassNotFoundException e) {
            isVeryOutdated = true;
        }

        if (!isVeryOutdated) {
            try {
                chatMessageTypeCreatorMethod = new MethodResolver(chatMessageTypeEnum).resolve(0, byte.class);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }

            try {
                chatClassConstructor = packetClass.getConstructor(iChatBaseComponentClass, chatMessageTypeEnum);
                constructorMode = 2;
            } catch (NoSuchMethodException e) {
                //Just a much newer version(1.16.x and above right now)
                try {
                    chatClassConstructor = packetClass.getConstructor(iChatBaseComponentClass, chatMessageTypeEnum, UUID.class);
                    constructorMode = 3;
                } catch (NoSuchMethodException e2) {
                    //Failed to resolve the constructor
                    e2.printStackTrace();
                }
            }
        } else {
            try {
                chatClassConstructor = packetClass.getConstructor(iChatBaseComponentClass, byte.class);
                constructorMode = 0;
            } catch (NoSuchMethodException e) {
                //That is fine, they are most likely on an older version.
                try {
                    chatClassConstructor = packetClass.getConstructor(iChatBaseComponentClass, int.class);
                    constructorMode = 1;
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                }
            }
        }

        cachedChatPositions = ImmutableMap.<ChatPosition, Byte>builder()
                .put(ChatPosition.CHAT, (byte) 0)
                .put(ChatPosition.SYSTEM_MESSAGE, (byte) 1)
                .put(ChatPosition.GAME_INFO, (byte) 2)
                .build();

        cachedChatPositionIntegers = ImmutableMap.<Byte, ChatPosition>builder()
                .put((byte) 0, ChatPosition.CHAT)
                .put((byte) 1, ChatPosition.SYSTEM_MESSAGE)
                .put((byte) 2, ChatPosition.GAME_INFO)
                .build();

        cachedChatMessageTypeIntegers = ImmutableMap.<String, Byte>builder()
                .put("CHAT", (byte) 0)
                .put("SYSTEM", (byte) 1)
                .put("GAME_INFO", (byte) 2)
                .build();

    }

    public static String fromStringToJSON(String message) {
        return "{\"text\": \"" + message + "\"}";
    }

    public static String toStringFromIChatBaseComponent(Object obj) {
        try {
            return (String) new MethodResolver(iChatBaseComponentClass).resolve(String.class, 0).invoke(obj);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object toIChatBaseComponent(String msg) {
        try {
            return new MethodResolver(chatSerializerClass).resolve(0, String.class).invoke(null, msg);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void setup() {
        final Object iChatBaseObj = readObject(0, iChatBaseComponentClass);

        this.message = toStringFromIChatBaseComponent(iChatBaseObj);

        byte chatPosInteger = 0;
        switch (constructorMode) {
            case 0:
                chatPosInteger = readByte(0);
                break;
            case 1:
                chatPosInteger = (byte) readInt(0);
                break;
            case 2:
            case 3:
                Object chatTypeEnumInstance = readObject(0, chatMessageTypeEnum);
                chatPosInteger = cachedChatMessageTypeIntegers.get(chatTypeEnumInstance.toString());
                break;
        }
        this.chatPosition = cachedChatPositionIntegers.get(chatPosInteger);
    }

    @Override
    public Object asNMSPacket() {
        int integerChatPos = cachedChatPositions.get(chatPosition);
        Object chatMessageTypeInstance = null;
        if (chatMessageTypeEnum != null) {
            if (chatTypeMessageEnumConstructorMode == 0) {
                chatMessageTypeInstance = chatMessageTypeCreatorMethod.invoke(null, (byte) integerChatPos);
            } else if (chatTypeMessageEnumConstructorMode == 1) {
                chatMessageTypeInstance = chatMessageTypeCreatorMethod.invoke(null, (byte) integerChatPos);
            }
        }
        switch (constructorMode) {
            case 0:
                try {
                    return chatClassConstructor.newInstance(toIChatBaseComponent(this.message), (byte) integerChatPos);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    return chatClassConstructor.newInstance(toIChatBaseComponent(this.message), integerChatPos);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    return chatClassConstructor.newInstance(toIChatBaseComponent(this.message), chatMessageTypeInstance);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    return chatClassConstructor.newInstance(toIChatBaseComponent(this.message), chatMessageTypeInstance, uuid);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    /**
     * Get the chat position.
     *
     * On 1.7.10, Only CHAT and SYSTEM_MESSAGE exist.
     * If an invalid chat position is sent, it will be defaulted it to CHAT.
     * @return ChatPosition
     */
    @NonNull
    public ChatPosition getChatPosition() {
        if(this.chatPosition == null) {
            this.chatPosition = ChatPosition.CHAT;
        }
        return this.chatPosition;
    }
}
