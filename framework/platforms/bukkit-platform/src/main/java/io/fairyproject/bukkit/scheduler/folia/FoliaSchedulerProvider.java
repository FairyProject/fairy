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

package io.fairyproject.bukkit.scheduler.folia;

import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.scheduler.MCScheduler;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import io.fairyproject.mc.util.Position;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class FoliaSchedulerProvider implements MCSchedulerProvider {

    private final Plugin plugin;

    @Override
    public MCScheduler getGlobalScheduler() {
        return new FoliaGlobalRegionScheduler(plugin);
    }

    @Override
    public MCScheduler getAsyncScheduler() {
        return new FoliaAsyncScheduler(plugin);
    }

    @Override
    public MCScheduler getEntityScheduler(Object entity) {
        if (entity instanceof Entity) {
            return new FoliaEntityScheduler((Entity) entity, plugin);
        }
        throw new IllegalArgumentException("entity must be an instance of org.bukkit.entity.Entity");
    }

    @Override
    public MCScheduler getLocationScheduler(Position position) {
        return new FoliaRegionScheduler(plugin, BukkitPos.toBukkitLocation(position));
    }

    @Override
    public MCScheduler getChunkScheduler(MCWorld world, int chunkX, int chunkZ) {
        return new FoliaRegionScheduler(plugin, world.as(World.class), chunkX, chunkZ);
    }
}
