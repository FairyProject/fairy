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

import io.fairyproject.bean.Beans;
import io.fairyproject.bukkit.packet.PacketService;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.SendableWrapper;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class WrappedPacketOutCustomPayload extends WrappedPacket implements SendableWrapper {

    private static Class<?> packetClass;
    private static Constructor<?> constructor;
    private static Constructor<?> packetDataSerializerConstructor;
    private static Constructor<?> minecraftKeyConstructor;
    private static Class<?> byteBufClass;
    private static Class<?> unpooledClass;
    private static Class<?> packetDataSerializerClass;
    private static Class<?> minecraftKeyClass;

    private static byte constructorMode = 1;

    public static void init() {
        packetClass = PacketTypeClasses.Server.CUSTOM_PAYLOAD;
        packetDataSerializerClass = NMS_CLASS_RESOLVER.resolveSilent("PacketDataSerializer");
        minecraftKeyClass = NMS_CLASS_RESOLVER.resolveSilent("MinecraftKey");

        try {
            unpooledClass = NMS_CLASS_RESOLVER.resolve("buffer.Unpooled");
            byteBufClass = NMS_CLASS_RESOLVER.resolve("buffer.ByteBuf");
        } catch (ClassNotFoundException e) {

        }
        try {
            packetDataSerializerConstructor = packetDataSerializerClass.getConstructor(NETTY_CLASS_RESOLVER.resolve("ByteBuf"));
        } catch (NullPointerException e) {
            //Nothing
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            //also nothing
        }

        try {
            minecraftKeyConstructor = minecraftKeyClass.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            //Nothing
        }

        //Constructors:

        //String, byte[]

        //String, PacketDataSerializer

        //MinecraftKey, PacketDataSerializer
        try {
            //1.7 constructor
            constructor = packetClass.getConstructor(String.class, byte[].class);
            constructorMode = 0;
        } catch (NoSuchMethodException e) {
            //That's fine, just a newer version
            try {
                constructor = packetClass.getConstructor(String.class, packetDataSerializerClass);
                constructorMode = 1;
            } catch (NoSuchMethodException e2) {
                //That's fine, just an even newer version
                try {
                    constructor = packetClass.getConstructor(minecraftKeyClass, packetDataSerializerClass);
                    constructorMode = 2;
                } catch (NoSuchMethodException e3) {
                    throw new IllegalStateException("PacketEvents is unable to resolve the PacketPlayOutCustomPayload constructor.");
                }
            }
        }
    }

    private String tag;
    private byte[] data;

    public WrappedPacketOutCustomPayload(String tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }


    public WrappedPacketOutCustomPayload(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {

        switch (constructorMode) {
            case 0:
                this.tag = this.readString(0);
                this.data = this.readObject(0,  byte[].class);
                break;
            default:
                this.tag = this.readString(0);
                Object byteBuf = this.readObject(0, packetDataSerializerClass);

                this.data = Beans.get(PacketService.class).getNettyInjection().readBytes(byteBuf);
                break;
        }

    }

    public String getTag() {
        return tag;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public Object asNMSPacket() {
        Object byteBufObject = new MethodResolver(unpooledClass)
                .resolveWrapper("copiedBuffer")
                .invoke(null, data);

        switch (constructorMode) {
            case 0:
                try {
                    return constructor.newInstance(tag, data);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    Object dataSerializer = packetDataSerializerConstructor.newInstance(byteBufObject);
                    return constructor.newInstance(tag, dataSerializer);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case 2:

                try {
                    Object minecraftKey = minecraftKeyConstructor.newInstance(tag);
                    Object dataSerializer = packetDataSerializerConstructor.newInstance(byteBufObject);
                    return constructor.newInstance(minecraftKey, dataSerializer);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
}
