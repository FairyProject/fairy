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

import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import io.fairyproject.bukkit.scheduler.folia.wrapper.WrapperScheduledTask;
import io.fairyproject.mc.scheduler.MCTickBasedScheduler;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.response.TaskResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class FoliaRegionScheduler extends FoliaAbstractScheduler implements MCTickBasedScheduler {

    private final Plugin bukkitPlugin;
    private final Location location;
    private final ObjectWrapper scheduler;
    private final Method isOwnedByCurrentRegion;

    public FoliaRegionScheduler(Plugin bukkitPlugin, Location location) {
        this.bukkitPlugin = bukkitPlugin;
        this.location = location;
        this.scheduler = this.getWrapScheduler("getRegionScheduler");
        try {
            isOwnedByCurrentRegion = Bukkit.class.getMethod("isOwnedByCurrentRegion", Location.class);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot find isOwnedByCurrentRegion method in Bukkit", e);
        }
    }

    @Override
    public boolean isCurrentThread() {
        try {
            return (boolean) isOwnedByCurrentRegion.invoke(null, location);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot invoke isOwnedByCurrentRegion method in Bukkit", e);
        }
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        return doSchedule(callable, task -> scheduler.invoke("run", bukkitPlugin, location, task));
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, long delayTicks) {
        return doSchedule(callable, task -> scheduler.invoke("runDelayed", bukkitPlugin, location, task, delayTicks));
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, long delayTicks, long intervalTicks) {
        FoliaRepeatedScheduledTask<R> task = new FoliaRepeatedScheduledTask<>(callback);
        Object rawScheduledTask = scheduler.invoke("runAtFixedRate", bukkitPlugin, location, task, delayTicks, intervalTicks);
        task.setScheduledTask(WrapperScheduledTask.of(rawScheduledTask));

        return task;
    }
}
