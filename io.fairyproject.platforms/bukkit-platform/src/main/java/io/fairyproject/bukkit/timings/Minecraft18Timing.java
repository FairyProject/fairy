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

package io.fairyproject.bukkit.timings;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * I thought that changing return type was ABI safe, and in 1.9 I changed it from
 * void to Timing....
 *
 * Well I was wrong, so for 1.8 servers, we need to use reflection to get the void return type instead.
 *
 * @author aikar
 */
class Minecraft18Timing extends MCTiming {
    private final Object timing;
    private static MethodHandle startTiming;
    private static MethodHandle stopTiming;
    private static MethodHandle of;

    static {
        try {
            Class<?> timing = Class.forName("co.aikar.timings.Timing");
            Class<?> timings = Class.forName("co.aikar.timings.Timings");
            final Method startTimingIfSync = timing.getDeclaredMethod("startTimingIfSync");
            startTiming = MethodHandles.lookup().unreflect(startTimingIfSync);
            final Method stopTimingIfSync = timing.getDeclaredMethod("stopTimingIfSync");
            stopTiming = MethodHandles.lookup().unreflect(stopTimingIfSync);
            final Method of1 = timings.getDeclaredMethod("of", Plugin.class, String.class, timing);
            of = MethodHandles.lookup().unreflect(of1);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Timings18 failed to initialize correctly. Stuff's going to be broken.");
        }
    }

    Minecraft18Timing(Plugin plugin, String name, MCTiming parent) throws Throwable {
        super();
        this.timing = of.invoke(null, plugin, name, parent instanceof Minecraft18Timing ? ((Minecraft18Timing) parent).timing : null);
    }

    @Override
    public MCTiming startTiming() {
        try {
            if (startTiming != null) {
                startTiming.invoke(timing);
            }
        } catch (Throwable ignored) {}
        return this;
    }

    @Override
    public void stopTiming() {
        try {
            if (stopTiming != null) {
                stopTiming.invoke(timing);
            }
        } catch (Throwable ignored) {}
    }
}
