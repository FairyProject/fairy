package io.fairyproject.data.impl;

import io.fairyproject.data.MetaKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MetaStorageImplTest {

    private MetaStorageImpl metaStorage;
    private MetaKey<Integer> testKey;

    @BeforeEach
    void setUp() {
        testKey = MetaKey.create("test", Integer.class);
        metaStorage = new MetaStorageImpl();
    }

    @Test
    void putAndGetOrNull() {
        assertNull(metaStorage.getOrNull(testKey), "Storage should return null for non-existing key");

        metaStorage.put(testKey, 123);
        assertEquals(123, metaStorage.getOrNull(testKey), "Storage should return the value previously put in");
    }

    @Test
    void computeIfAbsent() {
        metaStorage.computeIfAbsent(testKey, () -> 123);
        assertEquals(123, metaStorage.getOrNull(testKey), "Storage should contain the value put in if absent");

        metaStorage.computeIfAbsent(testKey, () -> 456);
        assertEquals(123, metaStorage.getOrNull(testKey), "Storage should not overwrite existing value with putIfAbsent");
    }

    @Test
    void remove() {
        assertFalse(metaStorage.remove(testKey), "Removing a non-existing key should return false");

        metaStorage.put(testKey, 123);
        assertTrue(metaStorage.remove(testKey), "Removing an existing key should return true");
        assertNull(metaStorage.getOrNull(testKey), "Storage should return null for removed key");
    }

    @Test
    void clear() {
        metaStorage.put(testKey, 123);
        assertNotNull(metaStorage.getOrNull(testKey), "Storage should return the value previously put in");

        metaStorage.clear();
        assertNull(metaStorage.getOrNull(testKey), "Storage should return null after clear");
    }

    @Test
    void putAndRetrieveReference() {
        WeakReference<Integer> weakReference = new WeakReference<>(123);
        metaStorage.putRef(testKey, weakReference);
        assertSame(123, metaStorage.getOrNull(testKey), "Storage should return the reference previously put in");
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        for (int k = 0; k < 10; k++) {
            MetaStorageImpl metaStorage = new MetaStorageImpl();
            MetaKey<Integer> testKey = MetaKey.create("testKey", Integer.class);
            int numberOfThreads = 1000;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch endGate = new CountDownLatch(numberOfThreads);
            AtomicReference<String> testFailed = new AtomicReference<>(null);
            AtomicInteger inserted = new AtomicInteger(-1);

            for (int i = 0; i < numberOfThreads; i++) {
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        startGate.await();
                        int value = metaStorage.computeIfAbsent(testKey, () -> finalI);

                        synchronized (inserted) {
                            if (inserted.get() == -1) {
                                inserted.set(value);
                            } else if (inserted.get() != value) {
                                testFailed.set("Inserted value does not match: " + inserted.get() + " != " + value);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endGate.countDown();
                    }
                });
            }

            startGate.countDown();
            endGate.await();
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);

            String failMessage = testFailed.get();
            assertNull(failMessage, failMessage);
        }
    }
}