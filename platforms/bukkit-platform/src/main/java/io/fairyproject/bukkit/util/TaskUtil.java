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

package io.fairyproject.bukkit.util;

import io.fairyproject.task.Task;
import org.bukkit.scheduler.BukkitTask;
import io.fairyproject.bukkit.FairyBukkitPlatform;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @deprecated Old structure
 * @see Task
 */
@Deprecated
public class TaskUtil {

    public static BukkitTask runAsync(Runnable runnable) {
        return FairyBukkitPlatform.PLUGIN.getServer().getScheduler().runTaskAsynchronously(FairyBukkitPlatform.PLUGIN, runnable);
    }

    public static BukkitTask runAsync(TaskRunnable runnable) {
        AtomicReference<BukkitTask> reference = new AtomicReference<>();
        BukkitTask task = runAsync(() -> runnable.run(reference.get()));

        reference.set(task);
        return task;
    }

    public static BukkitTask runAsyncScheduled(Runnable runnable, long time) {
        return FairyBukkitPlatform.PLUGIN.getServer().getScheduler().runTaskLaterAsynchronously(FairyBukkitPlatform.PLUGIN, runnable, time);
    }

    public static BukkitTask runAsyncScheduled(TaskRunnable runnable, int time) {
        AtomicReference<BukkitTask> reference = new AtomicReference<>();
        BukkitTask task = runAsyncScheduled(() -> runnable.run(reference.get()), time);

        reference.set(task);
        return task;
    }

    public static BukkitTask runAsyncRepeated(Runnable runnable, long time) {
        return FairyBukkitPlatform.PLUGIN.getServer().getScheduler().runTaskTimerAsynchronously(FairyBukkitPlatform.PLUGIN, runnable, time, time);
    }

    public static BukkitTask runAsyncRepeated(TaskRunnable runnable, int time) {
        AtomicReference<BukkitTask> reference = new AtomicReference<>();
        BukkitTask task = runAsyncRepeated(() -> runnable.run(reference.get()), time);

        reference.set(task);
        return task;
    }

    public static BukkitTask runSync(Runnable runnable) {
        return FairyBukkitPlatform.PLUGIN.getServer().getScheduler().runTask(FairyBukkitPlatform.PLUGIN, runnable);
    }

    public static BukkitTask runSync(TaskRunnable runnable) {
        AtomicReference<BukkitTask> reference = new AtomicReference<>();
        BukkitTask task = runSync(() -> runnable.run(reference.get()));

        reference.set(task);
        return task;
    }

    public static BukkitTask runScheduled(Runnable runnable, long time) {
        return FairyBukkitPlatform.PLUGIN.getServer().getScheduler().runTaskLater(FairyBukkitPlatform.PLUGIN, runnable, time);
    }

    public static BukkitTask runScheduled(TaskRunnable runnable, int time) {
        AtomicReference<BukkitTask> reference = new AtomicReference<>();
        BukkitTask task = runScheduled(() -> runnable.run(reference.get()), time);

        reference.set(task);
        return task;
    }

    public static BukkitTask runRepeated(Runnable runnable, long time) {
        return FairyBukkitPlatform.PLUGIN.getServer().getScheduler().runTaskTimer(FairyBukkitPlatform.PLUGIN, runnable, time, time);
    }

    public static BukkitTask runRepeated(TaskRunnable runnable, int time) {
        AtomicReference<BukkitTask> reference = new AtomicReference<>();
        BukkitTask task = runRepeated(() -> runnable.run(reference.get()), time);

        reference.set(task);
        return task;
    }

}
