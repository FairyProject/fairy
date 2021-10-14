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

package io.fairyproject.bukkit.packet.wrapper.client;

import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import lombok.Getter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;

@Getter
@AutowiredWrappedPacket(value = PacketType.Client.ABILITIES, direction = PacketDirection.READ)
public final class WrappedPacketInAbilities extends WrappedPacket {

    private static final boolean MULTIPLE_ABILITIES;

    static {
        MULTIPLE_ABILITIES = new FieldResolver(PacketTypeClasses.Client.ABILITIES)
                .resolveSilent(boolean.class, 1)
                .get(null);
    }

    private boolean vulnerable;
    private boolean flying;
    private boolean allowFly;
    private boolean instantBuild;
    private float flySpeed;
    private float walkSpeed;

    public WrappedPacketInAbilities(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        if (MULTIPLE_ABILITIES) {
            this.vulnerable = readBoolean(0);
            this.flying = readBoolean(1);
            this.allowFly = readBoolean(2);
            this.instantBuild = readBoolean(3);
            this.flySpeed = readFloat(0);
            this.walkSpeed = readFloat(1);
        } else {
            this.flying = readBoolean(0);
        }
    }

    public void setVulnerable(boolean vulnerable) {
        this.validBasePacketExists();

        this.packet.setFieldByIndex(boolean.class, 0, vulnerable);
        this.vulnerable = vulnerable;
    }
}
