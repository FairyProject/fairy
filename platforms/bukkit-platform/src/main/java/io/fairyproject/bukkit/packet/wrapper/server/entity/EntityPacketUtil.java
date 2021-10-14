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

package io.fairyproject.bukkit.packet.wrapper.server.entity;

import lombok.Getter;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;

import java.lang.reflect.Field;

public class EntityPacketUtil {

    //Byte = 1.7.10->1.8.8, Int = 1.9->1.15.x, Short = 1.16.x
    @Getter private static byte mode = 0; //byte = 0, int = 1, short = 2
    @Getter private static double dXYZDivisor = 0.0;

    public static void init() {
        Class<?> packetClass = PacketTypeClasses.Server.ENTITY;

        try {
            FieldResolver fieldResolver = new FieldResolver(packetClass);
            Field dxField = fieldResolver.resolveIndex(1);
            assert dxField != null;
            if (dxField.equals(fieldResolver.resolve(byte.class, 0).getField())) {
                mode = 0;
            } else if (dxField.equals(fieldResolver.resolve(int.class, 1).getField())) {
                mode = 1;
            } else if (dxField.equals(fieldResolver.resolve(short.class, 0).getField())) {
                mode = 2;
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        if (mode == 0) {
            dXYZDivisor = 32.0;
        } else {
            dXYZDivisor = 4096.0;
        }

    }

}
