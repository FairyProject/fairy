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

import org.fairy.task.ITaskScheduler;
import org.fairy.bukkit.Imanity;

public class BukkitTaskScheduler implements ITaskScheduler {
    @Override
    public int runAsync(Runnable runnable) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskAsynchronously(Imanity.PLUGIN, runnable).getTaskId();
    }

    @Override
    public int runAsyncScheduled(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskLaterAsynchronously(Imanity.PLUGIN, runnable, time).getTaskId();
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimerAsynchronously(Imanity.PLUGIN, runnable, time, time).getTaskId();
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long delay, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimerAsynchronously(Imanity.PLUGIN, runnable, delay, time).getTaskId();
    }

    @Override
    public int runSync(Runnable runnable) {
        return Imanity.PLUGIN.getServer().getScheduler().runTask(Imanity.PLUGIN, runnable).getTaskId();
    }

    @Override
    public int runScheduled(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskLater(Imanity.PLUGIN, runnable, time).getTaskId();
    }

    @Override
    public int runRepeated(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimer(Imanity.PLUGIN, runnable, time, time).getTaskId();
    }

    @Override
    public int runRepeated(Runnable runnable, long delay, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimer(Imanity.PLUGIN, runnable, delay, time).getTaskId();
    }

    @Override
    public void cancel(int taskId) {
        Imanity.PLUGIN.getServer().getScheduler().cancelTask(taskId);
    }
}
