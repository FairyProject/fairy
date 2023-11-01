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

import io.fairyproject.mc.scheduler.MCTickBasedScheduler;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.response.TaskResponse;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class FoliaRegionScheduler extends FoliaAbstractScheduler implements MCTickBasedScheduler {

    private final RegionScheduler scheduler = Bukkit.getRegionScheduler();
    private final Location location;
    private final Plugin bukkitPlugin;

    @Override
    public boolean isCurrentThread() {
        return Bukkit.isOwnedByCurrentRegion(location);
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        return doSchedule(callable, task -> scheduler.run(bukkitPlugin, location, task));
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, long delayTicks) {
        return doSchedule(callable, task -> scheduler.runDelayed(bukkitPlugin, location, task, delayTicks));
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, long delayTicks, long intervalTicks) {
        FoliaRepeatedScheduledTask<R> task = new FoliaRepeatedScheduledTask<>(callback);
        task.setScheduledTask(scheduler.runAtFixedRate(bukkitPlugin, location, task, delayTicks, intervalTicks));

        return task;
    }
}
