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

package org.fairy.bukkit.impl;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.fairy.bukkit.FairyBukkitPlatform;
import org.fairy.task.ITaskScheduler;
import org.fairy.bukkit.Imanity;
import org.fairy.task.TaskRunnable;
import org.fairy.util.terminable.Terminable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BukkitTaskScheduler implements ITaskScheduler {
    @Override
    public Terminable runAsync(Runnable runnable) {
        return this.toTerminable(FairyBukkitPlatform.INSTANCE.getServer().getScheduler().runTaskAsynchronously(FairyBukkitPlatform.INSTANCE, runnable));
    }

    @Override
    public Terminable runAsyncScheduled(Runnable runnable, long time) {
        return this.toTerminable(FairyBukkitPlatform.INSTANCE.getServer().getScheduler().runTaskLaterAsynchronously(FairyBukkitPlatform.INSTANCE, runnable, time));
    }

    @Override
    public Terminable runAsyncRepeated(TaskRunnable runnable, long time) {
        return this.runAsyncRepeated(runnable, time, time);
    }

    @Override
    public Terminable runAsyncRepeated(TaskRunnable runnable, long delay, long time) {
        AtomicReference<Terminable> terminable = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);
        final Terminable instance = this.toTerminable(FairyBukkitPlatform.INSTANCE.getServer().getScheduler().runTaskTimerAsynchronously(FairyBukkitPlatform.INSTANCE, () -> {
            if (closed.get()) {
                return;
            }

            runnable.run(terminable.get());
        }, delay, time));
        terminable.set(instance);
        return instance;
    }

    @Override
    public Terminable runSync(Runnable runnable) {
        AtomicBoolean closed = new AtomicBoolean();
        return this.toTerminable(FairyBukkitPlatform.INSTANCE.getServer().getScheduler().runTask(FairyBukkitPlatform.INSTANCE, () -> {
            if (closed.compareAndSet(false, true)) {
                runnable.run();
            }
        }), closed);
    }

    @Override
    public Terminable runScheduled(Runnable runnable, long time) {
        AtomicBoolean closed = new AtomicBoolean();
        return this.toTerminable(FairyBukkitPlatform.INSTANCE.getServer().getScheduler().runTaskLater(FairyBukkitPlatform.INSTANCE, () -> {
            if (closed.compareAndSet(false, true)) {
                runnable.run();
            }
        }, time), closed);
    }

    @Override
    public Terminable runRepeated(TaskRunnable runnable, long time) {
        return this.runRepeated(runnable, time, time);
    }

    @Override
    public Terminable runRepeated(TaskRunnable runnable, long delay, long time) {
        AtomicReference<Terminable> terminable = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);
        final Terminable instance = this.toTerminable(FairyBukkitPlatform.INSTANCE.getServer().getScheduler().runTaskTimer(FairyBukkitPlatform.INSTANCE, () -> {
            if (closed.get()) {
                return;
            }

            runnable.run(terminable.get());
        }, delay, time));
        terminable.set(instance);
        return instance;
    }

    private Terminable toTerminable(BukkitTask bukkitTask) {
        return this.toTerminable(bukkitTask, new AtomicBoolean());
    }

    private Terminable toTerminable(BukkitTask bukkitTask, AtomicBoolean closed) {
        return new Terminable() {
            @Override
            public void close() {
                if (!closed.compareAndSet(false, true)) {
                    return;
                }

                bukkitTask.cancel();
            }

            @Override
            public boolean isClosed() {
                return closed.get();
            }
        };
    }
}
