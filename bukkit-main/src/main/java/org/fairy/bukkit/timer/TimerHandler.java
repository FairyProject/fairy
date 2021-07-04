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

package org.fairy.bukkit.timer;

import com.google.common.collect.Sets;
import org.bukkit.scheduler.BukkitTask;
import org.fairy.bukkit.util.TaskUtil;
import org.fairy.bean.PostDestroy;
import org.fairy.bean.PostInitialize;
import org.fairy.bean.Service;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.timer.event.TimerStartEvent;

import java.util.Iterator;
import java.util.Set;

@Service(name = "timer")
public class TimerHandler implements Runnable {

    private Set<Timer> timers;

    private BukkitTask task;

    @PostInitialize
    public void init() {
        this.timers = Sets.newConcurrentHashSet();
        this.task = TaskUtil.runRepeated(this, 5L);
    }

    @PostDestroy
    public void stop() {
        this.task.cancel();
    }

    public void add(Timer timer) {
        TimerStartEvent event = new TimerStartEvent(timer);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        synchronized (this) {
            this.timers.add(timer);
        }
        timer.start();
    }

    public void clear(Timer timer) {
        synchronized (this) {
            this.timers.remove(timer);
        }
    }

    public void clear(Class<? extends Timer> timerClass) {
        synchronized (this) {
            this.timers.removeIf(timerClass::isInstance);
        }
    }

    public boolean isTimerRunning(Class<? extends Timer> timerClass) {
        return this.getTimer(timerClass) != null;
    }

    public <T extends Timer> T getTimer(Class<T> timerClass) {
        synchronized (this) {
            return (T) this.timers
                    .stream()
                    .filter(timerClass::isInstance)
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public void run() {

        synchronized (this) {
            Iterator<Timer> iterator = this.timers.iterator();

            while (iterator.hasNext()) {

                Timer timer = iterator.next();

                if (!timer.isPaused()) {

                    timer.tick();
                    if (timer.isTimerElapsed() && timer.finish()) {
                        timer.clear(false);
                        iterator.remove();
                    }

                }
            }
        }
    }
}
