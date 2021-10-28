package io.fairyproject.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PreProcessBatch {

    public static PreProcessBatch create() {
        return new PreProcessBatch();
    }

    private Map<String, Runnable> runnableQueue = new ConcurrentHashMap<>();

    public void runOrQueue(String name, Runnable runnable) {
        synchronized (this) {
            if (this.runnableQueue == null) {
                runnable.run();
                return;
            }
            this.runnableQueue.put(name, runnable);
        }
    }

    public boolean remove(String name) {
        if (this.runnableQueue != null) {
            return this.runnableQueue.remove(name) != null;
        }
        return false;
    }

    public void flushQueue() {
        Map<String, Runnable> runnableQueue;
        synchronized (this) {
            runnableQueue = this.runnableQueue;
            this.runnableQueue = null;
        }

        for (Runnable runnable : runnableQueue.values()) {
            runnable.run();
        }
        runnableQueue.clear();
    }

}
