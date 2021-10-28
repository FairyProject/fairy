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

package io.fairyproject.bukkit.packet.wrapper.wrapped;

import io.fairyproject.bukkit.reflection.resolver.ClassResolver;
import lombok.Getter;
import lombok.SneakyThrows;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.util.MinecraftVersion;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.Method;
import java.util.Arrays;

@Getter
public enum WrappedEnumParticle {
    EXPLOSION_NORMAL,
    EXPLOSION_LARGE,
    EXPLOSION_HUGE,
    FIREWORKS_SPARK,
    WATER_BUBBLE,
    WATER_SPLASH,
    WATER_WAKE,
    SUSPENDED,
    SUSPENDED_DEPTH,
    CRIT,
    CRIT_MAGIC,
    SMOKE_NORMAL,
    SMOKE_LARGE,
    SPELL,
    SPELL_INSTANT,
    SPELL_MOB,
    SPELL_MOB_AMBIENT,
    SPELL_WITCH,
    DRIP_WATER,
    DRIP_LAVA,
    VILLAGER_ANGRY,
    VILLAGER_HAPPY,
    TOWN_AURA,
    NOTE,
    PORTAL,
    ENCHANTMENT_TABLE,
    FLAME,
    LAVA,
    CLOUD,
    REDSTONE,
    SNOWBALL,
    SNOW_SHOVEL,
    SLIME,
    HEART,
    BARRIER,
    ITEM_CRACK,
    BLOCK_CRACK,
    BLOCK_DUST,
    WATER_DROP,
    MOB_APPEARANCE,
    DRAGON_BREATH,
    END_ROD,
    DAMAGE_INDICATOR,
    SWEEP_ATTACK,
    FALLING_DUST,
    TOTEM,
    SPIT,
    SQUID_INK,
    BUBBLE_POP,
    CURRENT_DOWN,
    BUBBLE_COLUMN_UP,
    NAUTILUS,
    DOLPHIN,
    LEGACY_BLOCK_CRACK,
    LEGACY_BLOCK_DUST,
    LEGACY_FALLING_DUST;

    private static final NMSClassResolver PARTICLE_CLASS_RESOLVER = new NMSClassResolver();
    private static final OBCClassResolver CRAFT_BUKKIT_PARTICLE_CLASS_RESOLVER = new OBCClassResolver();
    private static final ClassResolver BUKKIT_PARTICLE_CLASS_RESOLVER = new ClassResolver();

    private static final Class<?> BUKKIT_PARTICLE_CLASS = BUKKIT_PARTICLE_CLASS_RESOLVER.resolveSilent("org.bukkit.Particle");

    private static Method CRAFT_PARTICLE_TO_NMS = null, ENUM_PARTICLE_VALUE_OF = null, BUKKIT_PARTICLE_VALUE_OF;

    public static WrappedEnumParticle getByName(String name) {
        return Arrays.stream(values()).filter(val -> val.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    @SneakyThrows
    public <T> T toNMS() {
        if(MinecraftVersion.olderThan(MinecraftVersion.V.v1_9)) {
            return (T) ENUM_PARTICLE_VALUE_OF.invoke(null, name());
        }

        return (T) CRAFT_PARTICLE_TO_NMS.invoke(null, BUKKIT_PARTICLE_VALUE_OF.invoke(null, name()));
    }

    public String getName() {
        String name = this.name();

        if(MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
            name = name.replace("LEGACY_", "");
        }

        return name;
    }

    static {
        if(MinecraftVersion.newerThan(MinecraftVersion.V.v1_9)) {
            try {
                BUKKIT_PARTICLE_VALUE_OF = BUKKIT_PARTICLE_CLASS.getMethod("valueOf");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Class<?> craftParticleClass = CRAFT_BUKKIT_PARTICLE_CLASS_RESOLVER.resolveSilent("CraftParticle");
            CRAFT_PARTICLE_TO_NMS = new MethodResolver(craftParticleClass).resolveSilent("toNMS");
        } else {
            try {
                ENUM_PARTICLE_VALUE_OF = PARTICLE_CLASS_RESOLVER.resolveSilent(MinecraftVersion.olderThan(MinecraftVersion.V.v1_13) ? "EnumParticle" : "Particle").getDeclaredMethod("valueOf");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
