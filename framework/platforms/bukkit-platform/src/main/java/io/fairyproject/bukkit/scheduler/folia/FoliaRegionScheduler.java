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

import io.fairyproject.bukkit.scheduler.folia.wrapper.WrapperScheduledTask;
import io.fairyproject.mc.scheduler.MCTickBasedScheduler;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.response.TaskResponse;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

public class FoliaRegionScheduler extends FoliaAbstractScheduler implements MCTickBasedScheduler {

    private final Plugin bukkitPlugin;
    private final World world;
    private final int x;
    private final int z;
    private final RegionScheduler scheduler;

    public FoliaRegionScheduler(Plugin bukkitPlugin, Location location) {
        this.bukkitPlugin = bukkitPlugin;
        this.world = location.getWorld();
        this.x = location.getBlockX() >> 4;
        this.z = location.getBlockZ() >> 4;
        this.scheduler = Bukkit.getRegionScheduler();
    }

    public FoliaRegionScheduler(Plugin bukkitPlugin, World world, int x, int z) {
        this.bukkitPlugin = bukkitPlugin;
        this.world = world;
        this.x = x;
        this.z = z;
        this.scheduler = Bukkit.getRegionScheduler();
    }

    @Override
    public boolean isCurrentThread() {
        try {
            return Bukkit.isOwnedByCurrentRegion(world, x, z);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot invoke isOwnedByCurrentRegion method in Bukkit", e);
        }
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        return doSchedule(callable, task -> scheduler.run(bukkitPlugin, world, x, z, task));
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, long delayTicks) {
        return doSchedule(callable, task -> scheduler.runDelayed(bukkitPlugin, world, x, z, task, delayTicks));
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, long delayTicks, long intervalTicks) {
        FoliaRepeatedScheduledTask<R> task = new FoliaRepeatedScheduledTask<>(callback);
        io.papermc.paper.threadedregions.scheduler.ScheduledTask rawScheduledTask = scheduler.runAtFixedRate(bukkitPlugin, world, x, z, task, delayTicks, intervalTicks);
        task.setScheduledTask(WrapperScheduledTask.of(rawScheduledTask));

        return task;
    }
}
