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
import io.fairyproject.mc.scheduler.MCMillisBasedScheduler;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.response.TaskResponse;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class FoliaAsyncScheduler extends FoliaAbstractScheduler implements MCMillisBasedScheduler {

    private final ObjectWrapper scheduler;
    private final Plugin bukkitPlugin;

    public FoliaAsyncScheduler(Plugin bukkitPlugin) {
        this.bukkitPlugin = bukkitPlugin;
        this.scheduler = this.getWrapScheduler("getAsyncScheduler");
    }

    @Override
    public boolean isCurrentThread() {
        return Thread.currentThread().getName().contains("Folia Async Scheduler Thread");
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        return doSchedule(callable, task -> scheduler.invoke("runNow", bukkitPlugin, task));
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, Duration delay) {
        return doSchedule(callable, task -> scheduler.invoke("runDelayed", bukkitPlugin, task, delay.toNanos(), TimeUnit.NANOSECONDS));
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, Duration delayTicks, Duration intervalTicks) {
        FoliaRepeatedScheduledTask<R> task = new FoliaRepeatedScheduledTask<>(callback);
        WrapperScheduledTask rawScheduledTask = scheduler.invoke("runAtFixedRate", bukkitPlugin, task, delayTicks.toNanos(), intervalTicks.toNanos(), TimeUnit.NANOSECONDS);
        task.setScheduledTask(WrapperScheduledTask.of(rawScheduledTask));

        return task;
    }
}
