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

package io.fairyproject.mc.scheduler;

import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.response.TaskResponse;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface MCMillisBasedScheduler extends MCScheduler {

    default ScheduledTask<?> schedule(Runnable runnable, long delayTicks) {
        return schedule(runnable, Duration.ofMillis(delayTicks * MILLISECONDS_PER_TICK));
    }

    default ScheduledTask<?> scheduleAtFixedRate(Runnable runnable, long delayTicks, long intervalTicks) {
        return scheduleAtFixedRate(runnable, Duration.ofMillis(delayTicks * MILLISECONDS_PER_TICK), Duration.ofMillis(intervalTicks * MILLISECONDS_PER_TICK));
    }

    default <R> ScheduledTask<R> schedule(Callable<R> callable, long delayTicks) {
        return schedule(callable, Duration.ofMillis(delayTicks * MILLISECONDS_PER_TICK));
    }

    default <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, long delayTicks, long intervalTicks) {
        return scheduleAtFixedRate(callback, Duration.ofMillis(delayTicks * MILLISECONDS_PER_TICK), Duration.ofMillis(intervalTicks * MILLISECONDS_PER_TICK));
    }

}
