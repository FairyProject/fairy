package io.fairyproject.data.impl;

import io.fairyproject.data.MetaKey;
import io.fairyproject.data.MetaStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

public class MetaStorageImpl implements MetaStorage {

    private static final int BUCKET_SIZE = 8;

    private final StampedLock[] buckets = new StampedLock[BUCKET_SIZE];
    private volatile Object[] data = new Object[MetaKey.getCurrentCapacity()];

    public MetaStorageImpl() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new StampedLock();
        }
    }

    private long[] lockAll() {
        long[] stamps = new long[buckets.length];
        for (int i = 0; i < buckets.length; i++) {
            stamps[i] = buckets[i].writeLock();
        }
        return stamps;
    }

    private void unlockAll(long[] stamps) {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i].unlockWrite(stamps[i]);
        }
    }

    protected StampedLock getBucket(int key) {
        int hash = Integer.hashCode(key);

        return this.buckets[getBucketCheckMinValue(hash)];
    }

    private int getBucketCheckMinValue(int hash) {
        return Math.abs(hash == Integer.MIN_VALUE ? 0 : hash) % BUCKET_SIZE;
    }

    private void ensureCapacity(int id) {
        if (id >= data.length) {
            long[] stamps = this.lockAll();
            try {
                // Check again in case another thread already resized the array
                if (id >= data.length) {
                    int newSize = Math.max(data.length * 2, id + 1);
                    Object[] newData = new Object[newSize];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    data = newData;
                }
            } finally {
                this.unlockAll(stamps);
            }
        }
    }

    @Override
    public <T> @Nullable T getOrNull(@NotNull MetaKey<T> key) {
        StampedLock bucket = this.getBucket(key.getId());
        long stamp = bucket.tryOptimisticRead();
        Object retVal = getOrNullInternal(key);

        if (bucket.validate(stamp)) {
            return key.cast(retVal);
        }

        stamp = bucket.readLock();
        try {
            return getOrNullInternal(key);
        } finally {
            bucket.unlockRead(stamp);
        }
    }

    private <T> @Nullable T getOrNullInternal(@NotNull MetaKey<T> key) {
        if (key.getId() >= data.length)
            return null;

        Object object = data[key.getId()];
        return key.cast(object);
    }

    @Override
    public <T> void put(@NotNull MetaKey<T> key, @NotNull T value) {
        putInternal(key, value);
    }

    @Override
    public <T> void putRef(@NotNull MetaKey<T> key, @NotNull Reference<T> reference) {
        putInternal(key, reference);
    }

    private void putInternal(@NotNull MetaKey<?> key, @NotNull Object value) {
        ensureCapacity(key.getId());

        StampedLock bucket = this.getBucket(key.getId());
        long stamp = bucket.writeLock();
        try {
            data[key.getId()] = value;
        } finally {
            bucket.unlockWrite(stamp);
        }
    }

    @Override
    public <T> T computeIfAbsent(@NotNull MetaKey<T> key, @NotNull Supplier<T> value) {
        return key.cast(putIfAbsentInternal(key, value));
    }

    @Override
    public <T> void computeIfAbsentRef(@NotNull MetaKey<T> key, @NotNull Supplier<Reference<T>> reference) {
        putIfAbsentInternal(key, reference);
    }

    private Object putIfAbsentInternal(@NotNull MetaKey<?> key, @NotNull Supplier<?> value) {
        ensureCapacity(key.getId());

        StampedLock bucket = this.getBucket(key.getId());
        long stamp = bucket.tryOptimisticRead();
        boolean write = false;
        try {
            for (; ; stamp = bucket.writeLock(), write = true) {
                if (stamp == 0L)
                    continue;

                Object object = data[key.getId()];
                if (!bucket.validate(stamp))
                    continue;

                if (object != null)
                    return object;

                stamp = bucket.tryConvertToWriteLock(stamp);
                if (stamp == 0L)
                    continue;

                write = true;
                Object o = value.get();
                data[key.getId()] = o;

                return o;
            }
        } finally {
            if (write) {
                bucket.unlock(stamp);
            }
        }
    }

    @Override
    public <T> boolean remove(@NotNull MetaKey<T> key) {
        StampedLock bucket = this.getBucket(key.getId());
        long stamp = bucket.tryOptimisticRead();
        boolean write = false;
        try {
            for (; ; stamp = bucket.writeLock(), write = true) {
                if (stamp == 0L)
                    continue;

                boolean noRemoval = key.getId() >= data.length || data[key.getId()] == null;

                if (!bucket.validate(stamp))
                    continue;

                if (noRemoval)
                    return false;

                stamp = bucket.tryConvertToWriteLock(stamp);
                if (stamp == 0L)
                    continue;

                write = true;
                data[key.getId()] = null;
                return true;
            }
        } finally {
            if (write) {
                bucket.unlock(stamp);
            }
        }
    }

    @Override
    public void clear() {
        long[] stamps = this.lockAll();
        try {
            this.data = new Object[MetaKey.getCurrentCapacity()];
        } finally {
            this.unlockAll(stamps);
        }
    }

}
