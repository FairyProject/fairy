package io.fairyproject.util.thread;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The queue that will block the current thread that calls {@link BlockingThreadAwaitQueue#await(BooleanSupplier)}
 * with busy wait, and you can add new tasks into the queue through {@link BlockingThreadAwaitQueue#execute(Runnable)}.
 * Whenever the BooleanSupplier in {@link BlockingThreadAwaitQueue#await(BooleanSupplier)} fails,
 * it will try to execute tasks in the queue and tries BooleanSupplier again and so on, until successful.
 */
public class BlockingThreadAwaitQueue implements Executor {

    private final Queue<Runnable> runnableQueue = new ConcurrentLinkedQueue<>();
    private final Object lock = new Object();
    private final Consumer<Throwable> exceptionHandler;

    private BlockingThreadAwaitQueue(Consumer<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public static BlockingThreadAwaitQueue create(Consumer<Throwable> exceptionHandler) {
        return new BlockingThreadAwaitQueue(exceptionHandler);
    }

    private boolean workingState = true;

    @Override
    public void execute(@NotNull Runnable runnable) {
        synchronized (this.lock) {
            if (!workingState) {
                runnable.run();
                return;
            }
            this.runnableQueue.add(runnable);
        }
    }

    public void await(@NotNull BooleanSupplier booleanSupplier) {
        while (!booleanSupplier.getAsBoolean()) {
            final Runnable runnable;
            synchronized (this.lock) {
                runnable = runnableQueue.poll();
            }
            if (runnable != null) {
                try {
                    runnable.run();
                } catch (Throwable throwable) {
                    this.exceptionHandler.accept(throwable);
                }
            }
        }

        synchronized (this.lock) {
            this.workingState = false;
            Runnable runnable;
            while ((runnable = runnableQueue.poll()) != null) {
                try {
                    runnable.run();
                } catch (Throwable throwable) {
                    this.exceptionHandler.accept(throwable);
                }
            }
        }
    }
}
