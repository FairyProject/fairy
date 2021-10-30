package io.fairyproject.app.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.task.TaskRunnable;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.terminable.Terminable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTaskScheduler implements ITaskScheduler {

    private final AtomicInteger id = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, AsyncTask> tasks = new ConcurrentHashMap<>();
    private final AtomicBoolean changed = new AtomicBoolean(false);

    private final Queue<AsyncTask> pending = new ArrayDeque<>();
    private final List<AsyncTask> temp = new ArrayList<>();

    private final ExecutorService threadedExecutorService;

    public AsyncTaskScheduler() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Task Scheduler Thread")
                .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
                .build()
        );
        this.threadedExecutorService = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Task Thread - %d")
                .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
                .build()
        );
        executorService.scheduleAtFixedRate(() -> {
            if (this.changed.get()) {
                this.parsePending();
            }

            AsyncTask task;
            while ((task = this.pending.poll()) != null) {
                task.setNext(task.getNext() - 1);
                if (task.getNext() == 0) {
                    try {
                        if (task.isAsync()) {
                            this.threadedExecutorService.submit(task.getRunnable());
                        } else {
                            task.getRunnable().run();
                        }
                    } catch (Throwable throwable) {
                        Stacktrace.print(throwable);
                    }

                    if (task.getPeriod() < 1) {
                        this.tasks.remove(task.getId());
                    } else {
                        task.setNext(task.getPeriod());
                        this.temp.add(task);
                    }
                }
            }

            this.pending.addAll(this.temp);
            this.temp.clear();

        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    private void parsePending() {
        this.pending.clear();
        this.pending.addAll(this.tasks.values());
    }

    public int handle(long period, boolean async, Runnable runnable) {
        return this.handle(period, period, async, runnable);
    }

    public int handle(long next, long period, boolean async, Runnable runnable) {
        int id = this.id.getAndIncrement();
        this.tasks.put(id, new AsyncTask(id, next, period, async, runnable));
        this.changed.set(true);

        return id;
    }

    public void cancel(int taskId) {
        this.tasks.remove(taskId);
        this.changed.set(true);
    }

    @Override
    public Terminable runAsync(Runnable runnable) {
        final int handle = this.handle(0L, -1L, true, runnable);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runAsyncScheduled(Runnable runnable, long time) {
        final int handle = this.handle(time, -1L, true, runnable);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runAsyncRepeated(TaskRunnable runnable, long time) {
        AtomicInteger idHandle = new AtomicInteger();
        final int handle = this.handle(time, true, () -> runnable.run(() -> this.cancel(idHandle.get())));
        idHandle.set(handle);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runAsyncRepeated(TaskRunnable runnable, long delay, long time) {
        AtomicInteger idHandle = new AtomicInteger();
        final int handle = this.handle(time, time, true, () -> runnable.run(() -> this.cancel(idHandle.get())));
        idHandle.set(handle);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runSync(Runnable runnable) {
        final int handle = this.handle(0L, -1L, false, runnable);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runScheduled(Runnable runnable, long time) {
        final int handle = this.handle(time, -1L, false, runnable);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runRepeated(TaskRunnable runnable, long time) {
        AtomicInteger idHandle = new AtomicInteger();
        final int handle = this.handle(time, false, () -> runnable.run(() -> this.cancel(idHandle.get())));
        idHandle.set(handle);
        return () -> this.cancel(handle);
    }

    @Override
    public Terminable runRepeated(TaskRunnable runnable, long delay, long time) {
        AtomicInteger idHandle = new AtomicInteger();
        final int handle = this.handle(time, time, false, () -> runnable.run(() -> this.cancel(idHandle.get())));
        idHandle.set(handle);
        return () -> this.cancel(handle);
    }
}
