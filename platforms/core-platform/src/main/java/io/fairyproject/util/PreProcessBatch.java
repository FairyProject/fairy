package io.fairyproject.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PreProcessBatch {

    public static PreProcessBatch create() {
        return new PreProcessBatch();
    }

    private Queue<Runnable> runnableQueue = new ConcurrentLinkedQueue<>();

    public void runOrQueue(Runnable runnable) {
        synchronized (this) {
            if (this.runnableQueue == null) {
                runnable.run();
                return;
            }
            this.runnableQueue.add(runnable);
        }
    }

    public void flushQueue() {
        Queue<Runnable> runnableQueue;
        synchronized (this) {
            runnableQueue = this.runnableQueue;
            this.runnableQueue = null;
        }

        Runnable runnable;
        while ((runnable = runnableQueue.poll()) != null) {
            runnable.run();
        }
    }

}
