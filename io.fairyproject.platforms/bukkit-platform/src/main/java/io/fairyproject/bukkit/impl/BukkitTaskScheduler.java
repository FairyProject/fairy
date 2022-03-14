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

package io.fairyproject.bukkit.impl;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.timings.MCTiming;
import io.fairyproject.bukkit.timings.TimingService;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.container.Autowired;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.task.TaskRunnable;
import io.fairyproject.util.terminable.Terminable;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BukkitTaskScheduler implements ITaskScheduler {

    @Autowired
    private static TimingService TIMING_SERVICE;

    @Override
    public Terminable runAsync(Runnable runnable) {
        return this.toTerminable(Bukkit.getServer().getScheduler().runTaskAsynchronously(FairyBukkitPlatform.PLUGIN, runnable));
    }

    @Override
    public Terminable runAsyncScheduled(Runnable runnable, long time) {
        return this.toTerminable(Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(FairyBukkitPlatform.PLUGIN, runnable, time));
    }

    @Override
    public Terminable runAsyncRepeated(TaskRunnable runnable, long time) {
        return this.runAsyncRepeated(runnable, time, time);
    }

    @Override
    public Terminable runAsyncRepeated(TaskRunnable runnable, long delay, long time) {
        AtomicReference<Terminable> terminable = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);
        final Terminable instance = this.toTerminable(Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(FairyBukkitPlatform.PLUGIN, () -> {
            if (closed.get()) {
                return;
            }

            runnable.run(terminable.get());
        }, delay, time));
        terminable.set(instance);
        return instance;
    }

    public Terminable runSync(Runnable runnable) {
        return this.runSync(runnable, runnable);
    }

    @Override
    public Terminable runSync(Runnable runnable, Object identifier) {
        AtomicBoolean closed = new AtomicBoolean();

        final Plugin plugin = this.getPlugin(identifier);
        final MCTiming timing = this.getTiming(identifier, plugin, -1);
        return this.toTerminable(Bukkit.getServer().getScheduler().runTask(plugin, () -> {
            if (closed.compareAndSet(false, true)) {
                timing.startTiming();
                try {
                    runnable.run();
                } finally {
                    timing.stopTiming();
                }
            }
        }), closed);
    }

    public Terminable runScheduled(Runnable runnable, long time) {
        return this.runScheduled(runnable, runnable, time);
    }

    @Override
    public Terminable runScheduled(Runnable runnable, Object identifier, long time) {
        AtomicBoolean closed = new AtomicBoolean();

        final Plugin plugin = this.getPlugin(identifier);
        final MCTiming timing = this.getTiming(identifier, plugin, -1);
        return this.toTerminable(Bukkit.getServer().getScheduler().runTaskLater(FairyBukkitPlatform.PLUGIN, () -> {
            if (closed.compareAndSet(false, true)) {
                timing.startTiming();
                try {
                    runnable.run();
                } finally {
                    timing.stopTiming();
                }
            }
        }, time), closed);
    }

    public Terminable runRepeated(TaskRunnable runnable, long time) {
        return this.runRepeated(runnable, runnable, time);
    }

    @Override
    public Terminable runRepeated(TaskRunnable runnable, Object identifier, long time) {
        return this.runRepeated(runnable, identifier, time, time);
    }

    public Terminable runRepeated(TaskRunnable runnable, long delay, long time) {
        return this.runRepeated(runnable, runnable, delay, time);
    }

    @Override
    public Terminable runRepeated(TaskRunnable runnable, Object identifier, long delay, long time) {
        AtomicReference<Terminable> terminable = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);

        final Plugin plugin = this.getPlugin(identifier);
        final MCTiming timing = this.getTiming(identifier, plugin, time);
        final Terminable instance = this.toTerminable(Bukkit.getServer().getScheduler().runTaskTimer(FairyBukkitPlatform.PLUGIN, () -> {
            if (closed.get()) {
                return;
            }

            timing.startTiming();
            try {
                runnable.run(terminable.get());
            } finally {
                timing.stopTiming();
            }
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

    private Plugin getPlugin(Object obj) {
        Plugin plugin = JavaPluginUtil.getProvidingPlugin(obj.getClass());
        if (plugin == null) {
            plugin = FairyBukkitPlatform.PLUGIN;
        }
        if (!plugin.isEnabled()) {
            LogManager.getLogger(Events.class).error("The plugin hasn't enabled but trying to register listener " + obj.getClass().getSimpleName());
        }
        return plugin;
    }

    private MCTiming getTiming(Object obj, Plugin plugin, long period) {
        final Class<?> taskClass = obj.getClass();
        String taskName;

        if (taskClass.isAnonymousClass()) {
            taskName = taskClass.getName();
        } else {
            taskName = taskClass.getCanonicalName();
        }

        String name = "Task: " + taskName;
        if (period > 0) {
            name += " (interval:" + period +")";
        } else {
            name += " (Single)";
        }

        return TIMING_SERVICE.of(plugin, name);
    }
}
