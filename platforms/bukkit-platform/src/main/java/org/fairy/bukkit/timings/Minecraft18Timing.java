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

package org.fairy.bukkit.timings;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

/**
 * I thought that changing return type was ABI safe, and in 1.9 I changed it from
 * void to Timing....
 *
 * Well I was wrong, so for 1.8 servers, we need to use reflection to get the void return type instead.
 *
 * @author aikar
 *
 * LeeGod: will... we are using TacoSpigot 1.8.8 as dependency so should be fine
 */
class Minecraft18Timing extends MCTiming {
//    private final Object timing;
//    private static Method startTiming;
//    private static Method stopTiming;
//    private static Method of;
//
//    static {
//        try {
//            Class<?> timing = Class.forName("co.aikar.timings.Timing");
//            Class<?> timings = Class.forName("co.aikar.timings.Timings");
//            startTiming = timing.getDeclaredMethod("startTimingIfSync");
//            stopTiming = timing.getDeclaredMethod("stopTimingIfSync");
//            of = timings.getDeclaredMethod("of", Plugin.class, String.class, timing);
//        } catch (ClassNotFoundException | NoSuchMethodException e) {
//            e.printStackTrace();
//            Bukkit.getLogger().severe("Timings18 failed to initialize correctly. Stuff's going to be broken.");
//        }
//    }

    private final Timing timing;

    Minecraft18Timing(Plugin plugin, String name, MCTiming parent) throws InvocationTargetException, IllegalAccessException {
        super();
        this.timing = Timings.of(plugin, name, parent instanceof Minecraft18Timing ? ((Minecraft18Timing) parent).timing : null);
//        this.timing = of.invoke(null, plugin, name, parent instanceof Minecraft18Timing ? ((Minecraft18Timing) parent).timing : null);
    }

    @Override
    public MCTiming startTiming() {
//        try {
//            if (startTiming != null) {
//                startTiming.invoke(timing);
//            }
//        } catch (IllegalAccessException | InvocationTargetException ignored) {}
        this.timing.startTimingIfSync();
        return this;
    }

    @Override
    public void stopTiming() {
//        try {
//            if (stopTiming != null) {
//                stopTiming.invoke(timing);
//            }
//        } catch (IllegalAccessException | InvocationTargetException ignored) {}
        this.timing.stopTimingIfSync();
    }
}
