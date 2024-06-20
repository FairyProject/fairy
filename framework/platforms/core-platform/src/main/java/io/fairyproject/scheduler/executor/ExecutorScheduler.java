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

package io.fairyproject.scheduler.executor;

import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.Scheduler;
import io.fairyproject.scheduler.response.TaskResponse;

import java.time.Duration;
import java.util.concurrent.*;

public class ExecutorScheduler implements Scheduler {

    private final ScheduledExecutorService executorService;
    private Thread thread;

    public ExecutorScheduler(ThreadFactory threadFactory) {
        this.executorService = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(runnable -> this.thread = threadFactory.newThread(runnable));
    }

    public ExecutorScheduler(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public boolean isCurrentThread() {
        return Thread.currentThread() == this.thread;
    }

    @Override
    public ScheduledTask<?> schedule(Runnable runnable) {
        return this.schedule(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public ScheduledTask<?> schedule(Runnable runnable, Duration delay) {
        return this.schedule(() -> {
            runnable.run();
            return null;
        }, delay);
    }

    @Override
    public ScheduledTask<?> scheduleAtFixedRate(Runnable runnable, Duration delay, Duration interval) {
        return this.scheduleAtFixedRate(() -> {
            runnable.run();
            return TaskResponse.continueTask();
        }, delay, interval);
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable) {
        SingleExecutorScheduledTask<R> task = new SingleExecutorScheduledTask<>(callable);
        task.setScheduledFuture(this.executorService.schedule(task, 0, TimeUnit.MILLISECONDS));

        return task;
    }

    @Override
    public <R> ScheduledTask<R> schedule(Callable<R> callable, Duration delay) {
        SingleExecutorScheduledTask<R> task = new SingleExecutorScheduledTask<>(callable);
        task.setScheduledFuture(this.executorService.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS));

        return task;
    }

    @Override
    public <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> callback, Duration delay, Duration interval) {
        RepeatedExecutorScheduledTask<R> task = new RepeatedExecutorScheduledTask<>(callback);
        task.setScheduledFuture(this.executorService.scheduleAtFixedRate(task, delay.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS));

        return task;
    }
}
