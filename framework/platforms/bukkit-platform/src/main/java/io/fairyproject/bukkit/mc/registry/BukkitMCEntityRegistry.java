/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.mc.registry;

import io.fairyproject.bukkit.mc.BukkitMCEntity;
import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.registry.MCEntityRegistry;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BukkitMCEntityRegistry implements MCEntityRegistry {

    private final BukkitDataWatcherConverter dataWatcherConverter;
    private final MCSchedulerProvider schedulerProvider;

    @Override
    public MCEntity convert(Object entity) {
        if (!(entity instanceof org.bukkit.entity.Entity)) {
            throw new UnsupportedOperationException();
        }

        return new BukkitMCEntity((org.bukkit.entity.Entity) entity, dataWatcherConverter, schedulerProvider);
    }
}
