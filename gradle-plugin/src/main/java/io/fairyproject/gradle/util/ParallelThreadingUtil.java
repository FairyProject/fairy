package io.fairyproject.gradle.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

@UtilityClass
public class ParallelThreadingUtil {

    private static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();

    public <T> void invokeAll(Collection<T> collection, ThrowingConsumer<T> consumer) {
        List<Callable<Void>> callables = new ArrayList<>();
        for (T t : collection) {
            callables.add(() -> {
                try {
                    consumer.accept(t);
                } catch (Throwable e) {
                    SneakyThrow.sneaky(e);
                }
                return null;
            });
        }
        try {
            EXECUTOR.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public <T> void invokeAll(Enumeration<T> collection,  ThrowingConsumer<T> consumer) {
        List<Callable<Void>> callables = new ArrayList<>();
        while (collection.hasMoreElements()) {
            final T entry = collection.nextElement();
            callables.add(() -> {
                try {
                    consumer.accept(entry);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
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

        private Enumeration<T> enumeration;
        private Collection<T> collection;
        private Function<T, Boolean> syncConsumer;
        private ThrowingConsumer<T> asyncConsumer;

        public ParallelTaskBuilder<T> collection(Enumeration<T> collection) {
            this.enumeration = collection;
            return this;
        }

        public ParallelTaskBuilder<T> collection(Collection<T> collection) {
            this.collection = collection;
            return this;
        }

        public ParallelTaskBuilder<T> syncConsumer(Function<T, Boolean> syncConsumer) {
            this.syncConsumer = syncConsumer;
            return this;
        }

        public ParallelTaskBuilder<T> asyncConsumer(ThrowingConsumer<T> consumer) {
            this.asyncConsumer = consumer;
            return this;
        }

        public void start() {
            List<Callable<Void>> callables = new ArrayList<>();
            if (enumeration != null) {
                while (enumeration.hasMoreElements()) {
                    final T entry = enumeration.nextElement();
                    if (!syncConsumer.apply(entry)) {
                        continue;
                    }
                    callables.add(() -> {
                        try {
                            asyncConsumer.accept(entry);
                        } catch (Throwable e) {
                            SneakyThrow.sneaky(e);
                        }
                        return null;
                    });
                }
            } else {
                for (T entry : this.collection) {
                    if (!syncConsumer.apply(entry)) {
                        continue;
                    }
                    callables.add(() -> {
                        try {
                            asyncConsumer.accept(entry);
                        } catch (Throwable e) {
                            SneakyThrow.sneaky(e);
                        }
                        return null;
                    });
                }
            }
            try {
                EXECUTOR.invokeAll(callables);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public interface ThrowingConsumer<T> {
        void accept(T t) throws Throwable;
    }

}
