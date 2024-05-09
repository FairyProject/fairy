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

import io.fairyproject.Debug;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersion;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@InjectableComponent
@RequiredArgsConstructor
public class TimingService {

    private final Map<String, MCTiming> timingCache = new HashMap<>(0);
    private final MCServer mcServer;

    private TimingType timingType;

    @PreInitialize
    public void onPreInitialize() {
        if (timingType == null) {
            if (Debug.UNIT_TEST) {
                timingType = TimingType.UNIT_TESTING;
                return;
            }
            try {
                Class<?> clazz = Class.forName("co.aikar.timings.Timing");
                Method startTiming = clazz.getMethod("startTiming");

                // ever since 1.19.4, aikar's timing has been deprecated
                if (mcServer.getVersion().isHigherOrEqual(MCVersion.of(19, 4))) {
                    timingType = TimingType.EMPTY;
                } else if (startTiming.getReturnType() != clazz) {
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
