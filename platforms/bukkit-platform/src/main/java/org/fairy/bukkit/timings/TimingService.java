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

import org.bukkit.plugin.Plugin;
import org.fairy.bean.PreInitialize;
import org.fairy.bean.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service(name = "timings")
public class TimingService {

    private TimingType timingType;
    private final Map<String, MCTiming> timingCache = new HashMap<>(0);

    @PreInitialize
    public void preInit() {
        if (timingType == null) {
            try {
                Class<?> clazz = Class.forName("co.aikar.timings.Timing");
                Method startTiming = clazz.getMethod("startTiming");
                if (startTiming.getReturnType() != clazz) {
                    timingType = TimingType.MINECRAFT_18;
                } else {
                    timingType = TimingType.MINECRAFT;
                }
            } catch (ClassNotFoundException | NoSuchMethodException ignored1) {
                try {
                    Class.forName("org.spigotmc.CustomTimingsHandler");
                    timingType = TimingType.SPIGOT;
                } catch (ClassNotFoundException ignored2) {
                    timingType = TimingType.EMPTY;
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public MCTiming ofStart(Plugin plugin, String name) {
        return ofStart(plugin, name, null);
    }

    @SuppressWarnings("WeakerAccess")
    public MCTiming ofStart(Plugin plugin, String name, MCTiming parent) {
        return of(plugin, name, parent).startTiming();
    }

    @SuppressWarnings("WeakerAccess")
    public MCTiming of(Plugin plugin, String name) {
        return of(plugin, name, null);
    }

    @SuppressWarnings("WeakerAccess")
    public MCTiming of(Plugin plugin, String name, MCTiming parent) {

        MCTiming timing;
        if (timingType.useCache()) {
            synchronized (timingCache) {
                String lowerKey = name.toLowerCase();
                timing = timingCache.get(lowerKey);
                if (timing == null) {
                    timing = timingType.newTiming(plugin, name, parent);
                    timingCache.put(lowerKey, timing);
                }
            }
            return timing;
        }

        return timingType.newTiming(plugin, name, parent);
    }
}
