package io.fairyproject.data.impl;

import io.fairyproject.data.MetaKey;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class MetaStorageImplBenchmark {

    public static final int keys = 1000;

    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State {
        private MetaKey<Integer>[] metaKeys;
        private MetadataKey<Integer>[] metadataKeys;

        @Setup(Level.Trial)
        public void setup() {
            MetaKey.ID_COUNTER.set(0);

            metaKeys = new MetaKey[keys];
            for (int i = 0; i < keys; i++) {
                metaKeys[i] = MetaKey.create("key" + i, Integer.class);
            }

            metadataKeys = new MetadataKey[keys];
            for (int i = 0; i < keys; i++) {
                metadataKeys[i] = MetadataKey.create("key" + i, Integer.class);
            }
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 10, time = 200, timeUnit = MILLISECONDS)
    public void metaStorage(State state) {
        MetaStorageImpl metaStorage = new MetaStorageImpl();

        for (int i = 0; i < state.metaKeys.length; i++) {
            metaStorage.put(state.metaKeys[i], i);
            metaStorage.getOrNull(state.metaKeys[i]);
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 10, time = 200, timeUnit = MILLISECONDS)
    public void metadataMap(State state) {
        MetadataMap metadataStorage = MetadataMap.create();

        for (int i = 0; i < state.metadataKeys.length; i++) {
            metadataStorage.put(state.metadataKeys[i], i);
            metadataStorage.get(state.metadataKeys[i]);
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 10, time = 200, timeUnit = MILLISECONDS)
    public void metaStorageConcurrent(State state) throws InterruptedException {
        MetaStorageImpl metaStorage = new MetaStorageImpl();
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int t = 0; t < 10; t++) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < state.metaKeys.length; i++) {
                        metaStorage.put(state.metaKeys[i], i);
                        metaStorage.getOrNull(state.metaKeys[i]);
                    }
                    countDownLatch.countDown();
                }
            }.start();
        }

        countDownLatch.await();
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 10, time = 200, timeUnit = MILLISECONDS)
    public void metadataMapConcurrent(State state) throws InterruptedException {
        MetadataMap metadataStorage = MetadataMap.create();
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int t = 0; t < 10; t++) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < state.metadataKeys.length; i++) {
                        metadataStorage.put(state.metadataKeys[i], i);
                        metadataStorage.get(state.metadataKeys[i]);
                    }
                    countDownLatch.countDown();
                }
            }.start();
        }

        countDownLatch.await();
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 10, time = 200, timeUnit = MILLISECONDS)
    public void concurrentHashMap(State state) throws InterruptedException {
        java.util.concurrent.ConcurrentHashMap<Integer, Integer> map = new java.util.concurrent.ConcurrentHashMap<>();

        for (int i = 0; i < state.metaKeys.length; i++) {
            map.put(state.metaKeys[i].getId(), i);
            map.get(state.metaKeys[i].getId());
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 10, time = 200, timeUnit = MILLISECONDS)
    public void concurrentHashMap_concurrent(State state) throws InterruptedException {
        java.util.concurrent.ConcurrentHashMap<Integer, Integer> map = new java.util.concurrent.ConcurrentHashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int t = 0; t < 10; t++) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < state.metaKeys.length; i++) {
                        map.put(state.metaKeys[i].getId(), i);
                        map.get(state.metaKeys[i].getId());
                    }
                    countDownLatch.countDown();
                }
            }.start();
        }

        countDownLatch.await();
    }

    public static void main(Object[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MetaStorageImplBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
