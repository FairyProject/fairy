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
import io.fairyproject.scheduler.repeat.RepeatPredicate;
import io.fairyproject.scheduler.response.TaskResponse;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class FoliaEntityScheduler extends FoliaAbstractScheduler implements MCTickBasedScheduler {

    private static Method isOwnedByCurrentRegion;

    private final Entity entity;
    private final Plugin bukkitPlugin;

    @Override
    public boolean isCurrentThread() {
        if (isOwnedByCurrentRegion == null) {
            try {
                isOwnedByCurrentRegion = Bukkit.class.getMethod("isOwnedByCurrentRegion", Entity.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Cannot find isOwnedByCurrentRegion method in Bukkit", e);
            }
        }

        try {
            return (boolean) isOwnedByCurrentRegion.invoke(null, entity);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot invoke isOwnedByCurrentRegion method in Bukkit", e);
        }
    }

    private EntityScheduler getEntityScheduler() {
        return entity.getScheduler();
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        return doSchedule(callable, task -> getEntityScheduler().run(bukkitPlugin, task, null));
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, long delayTicks) {
        return doSchedule(callable, task -> getEntityScheduler().runDelayed(bukkitPlugin, task, null, delayTicks));
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, long delayTicks, long intervalTicks, RepeatPredicate<R> predicate) {
        FoliaRepeatedScheduledTask<R> task = new FoliaRepeatedScheduledTask<>(callback, predicate);
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask = getEntityScheduler().runAtFixedRate(bukkitPlugin, task, null, delayTicks, intervalTicks);
        task.setScheduledTask(WrapperScheduledTask.of(scheduledTask));

        return task;
    }
}
