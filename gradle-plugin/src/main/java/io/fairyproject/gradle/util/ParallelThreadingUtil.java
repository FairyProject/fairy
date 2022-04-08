package io.fairyproject.gradle.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;

@UtilityClass
public class ParallelThreadingUtil {

    private static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();

    public <T> void invokeAll(Collection<T> collection, Consumer<T> consumer) {
        List<Callable<Void>> callables = new ArrayList<>();
        for (T t : collection) {
            callables.add(() -> {
                consumer.accept(t);
                return null;
            });
        }
        try {
            EXECUTOR.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public <T> void invokeAll(Enumeration<T> collection,  Consumer<T> consumer) {
        List<Callable<Void>> callables = new ArrayList<>();
        while (collection.hasMoreElements()) {
            final T entry = collection.nextElement();
            callables.add(() -> {
                consumer.accept(entry);
                return null;
            });
        }
        try {
            EXECUTOR.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public <T> ParallelTaskBuilder<T> newTask() {
        return new ParallelTaskBuilder<>();
    }

    public static class ParallelTaskBuilder<T> {

        private Enumeration<T> collection;
        private Function<T, Boolean> syncConsumer;
        private Consumer<T> asyncConsumer;

        public ParallelTaskBuilder<T> collection(Enumeration<T> collection) {
            this.collection = collection;
            return this;
        }

        public ParallelTaskBuilder<T> syncConsumer(Function<T, Boolean> syncConsumer) {
            this.syncConsumer = syncConsumer;
            return this;
        }

        public ParallelTaskBuilder<T> asyncConsumer(Consumer<T> consumer) {
            this.asyncConsumer = consumer;
            return this;
        }

        public void start() {
            List<Callable<Void>> callables = new ArrayList<>();
            while (collection.hasMoreElements()) {
                final T entry = collection.nextElement();
                if (!syncConsumer.apply(entry)) {
                    continue;
                }
                callables.add(() -> {
                    asyncConsumer.accept(entry);
                    return null;
                });
            }
            try {
                EXECUTOR.invokeAll(callables);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
