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

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.resolver.FieldResolver;
import org.fairy.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.fairy.util.AccessUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PacketWrapper extends WrapperAbstract {

    private static final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();

    public static PacketWrapper createByPacketName(String packetName) {
        try {
            ClassWrapper<?> classWrapper = new ClassWrapper<>(CLASS_RESOLVER.resolve(packetName));
            Object packet = classWrapper.newInstance();

            return new PacketWrapper(packet);
        } catch (Throwable throwable) {
            throw new RuntimeException("Unexpected error while creating packet wrapper", throwable);
        }
    }

    private final Object packetObject;
    private final FieldResolver fieldResolver;

    private boolean cached;

    @SneakyThrows
    public PacketWrapper(Object packetObject) {
        this.packetObject = packetObject;
        this.fieldResolver = new FieldResolver(this.packetObject.getClass());
        this.cached = true;
    }

    public PacketWrapper(Class<?> type) {
        try {
            this.packetObject = type.newInstance();
            this.fieldResolver = new FieldResolver(type);
            this.cached = true;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public PacketWrapper noCache() {
        this.cached = false;
        return this;
    }

    public Object getPacket() {
        return packetObject;
    }

    public void sendPacket(Player player) {
        MinecraftReflection.sendPacket(player, this.packetObject);
    }

    public PacketWrapper setPacketValueByType(Class<?> type, Object value) {
        try {
            FieldWrapper<Object> fieldWrapper = fieldResolver.resolveByFirstTypeWrapper(type);
            fieldWrapper.set(this.getPacket(), value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return this;
    }

    public PacketWrapper setPacketValue(String field, Object value) {
        try {
            fieldResolver.resolve(field).set(getPacket(), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Object getPacketValue(String field) {
        Object value = null;
        try {
            value = fieldResolver.resolve(field).get(getPacket());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public <T> FieldWrapper<T> getFieldByIndex(Class<T> type, int index) {
        return this.fieldResolver.resolve(type, index);
    }

    @SneakyThrows
    public PacketWrapper setFieldByIndex(Class<?> type, int index, Object value) {
        FieldWrapper field = this.getFieldByIndex(type, index);
        if (field == null) throw new RuntimeException("The field attempted to fetch with type " + type.getSimpleName() + " and index " + index + " on packet class " + this.packetObject.getClass().getSimpleName() + " does not exist");

        field.set(this.packetObject, value);
        return this;
    }

    @SneakyThrows
    public <T> T getPacketValueByIndex(Class<T> type, int index) {
        FieldWrapper<T> field = this.getFieldByIndex(type, index);
        if (field == null) throw new RuntimeException("The field attempted to fetch with type " + type.getSimpleName() + " and index " + index + " on packet class " + this.packetObject.getClass().getSimpleName() + " does not exist");

        return field.get(this.packetObject);
    }

    @Override
    public boolean exists() {
        return this.packetObject != null;
    }

    public List<String> getFields() {
        return Stream.of(this.packetObject.getClass().getDeclaredFields())
                .map(field -> {
                    try {
                        AccessUtil.setAccessible(field);

                        return field.getName() + ": " + field.get(this.packetObject);
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                })
                .collect(Collectors.toList());
    }
}
