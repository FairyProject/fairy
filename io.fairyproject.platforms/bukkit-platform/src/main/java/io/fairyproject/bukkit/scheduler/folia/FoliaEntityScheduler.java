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

import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import io.fairyproject.bukkit.scheduler.folia.wrapper.WrapperScheduledTask;
import io.fairyproject.mc.scheduler.MCTickBasedScheduler;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class FoliaEntityScheduler extends FoliaAbstractScheduler implements MCTickBasedScheduler {

    private static Method isOwnedByCurrentRegion;
    private static Method entityGetScheduler;

    private final Entity entity;
    private final Plugin bukkitPlugin;
    private ObjectWrapper scheduler;

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

    private static void ensureEntityGetScheduler() {
        if (entityGetScheduler != null)
            return;

        try {
            entityGetScheduler = Entity.class.getMethod("getScheduler");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find getScheduler method in Entity", e);
        }
    }

    private ObjectWrapper getEntityScheduler() {
        if (this.scheduler != null)
            return this.scheduler;

        ensureEntityGetScheduler();

        try {
            Object instance = entityGetScheduler.invoke(entity);
            return this.scheduler = new ObjectWrapper(instance);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot invoke getScheduler method in Entity", e);
        }
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        return doSchedule(callable, task -> {
            MethodWrapper<?> method = getEntityScheduler().getMethod("run", Plugin.class, Consumer.class, Runnable.class);

            return method.invoke(scheduler.getObject(), bukkitPlugin, task, null);
        });
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, long delayTicks) {
        return doSchedule(callable, task -> {
            MethodWrapper<?> method = getEntityScheduler().getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);

            return method.invoke(scheduler.getObject(), bukkitPlugin, task, null, delayTicks);
        });
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, long delayTicks, long intervalTicks) {
        FoliaRepeatedScheduledTask<R> task = new FoliaRepeatedScheduledTask<>(callback);
        MethodWrapper<?> method = getEntityScheduler().getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
        Object scheduledTask = method.invoke(scheduler.getObject(), bukkitPlugin, task, null, delayTicks, intervalTicks);
        task.setScheduledTask(WrapperScheduledTask.of(scheduledTask));

        return task;
    }
}
