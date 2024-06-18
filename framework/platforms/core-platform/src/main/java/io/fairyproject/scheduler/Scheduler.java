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

package io.fairyproject.scheduler;

import io.fairyproject.scheduler.repeat.RepeatPredicate;
import io.fairyproject.scheduler.response.TaskResponse;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public interface Scheduler extends Executor {

    boolean isCurrentThread();

    ScheduledTask<?> schedule(Runnable runnable);

    ScheduledTask<?> schedule(Runnable runnable, Duration delay);

    ScheduledTask<?> scheduleAtFixedRate(Runnable runnable, Duration delay, Duration interval);

    ScheduledTask<?> scheduleAtFixedRate(Runnable runnable, Duration delay, Duration interval, RepeatPredicate<?> predicate);

    <R> ScheduledTask<R> schedule(Callable<R> callable);

    <R> ScheduledTask<R> schedule(Callable<R> callable, Duration delay);

    <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, Duration delay, Duration interval);

    <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, Duration delay, Duration interval, RepeatPredicate<R> predicate);

    @Override
    default void execute(@NotNull Runnable runnable) {
        this.schedule(runnable);
    }

}
