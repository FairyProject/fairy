/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.fairy.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * The Simple Cooldown Utility for Fairy
 */
@Getter
public class Cooldown<T> {

    private final Cache<T, Long> cache;
    private final long defaultCooldown;
    private BiConsumer<T, RemovalCause> removalListener;

    public Cooldown(long defaultCooldown) {
        this(defaultCooldown, null);
    }

    public Cooldown(long defaultCooldown, BiConsumer<T, RemovalCause> removalListener) {
        this.removalListener = removalListener;
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<T, Long>() {
                    @Override
                    public long expireAfterCreate(@NonNull T key, @NonNull Long value, long currentTime) {
                        return TimeUnit.MILLISECONDS.toNanos(value - System.currentTimeMillis());
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull T key, @NonNull Long value, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.MILLISECONDS.toNanos(value - System.currentTimeMillis());
                    }

                    @Override
                    public long expireAfterRead(@NonNull T key, @NonNull Long value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .removalListener((key, value, cause) -> {
                    if (this.removalListener != null) {
                        this.removalListener.accept(key, cause);
                    }
                })
                .build();

        this.defaultCooldown = defaultCooldown;
    }

    public void removalListener(BiConsumer<T, RemovalCause> consumer) {
        this.removalListener = consumer;
    }

    public void addCooldown(T t, long cooldown) {
        this.cache.put(t, System.currentTimeMillis() + cooldown);
    }

    public void addCooldown(T t) {
        this.addCooldown(t, this.defaultCooldown);
    }

    public long getCooldown(T t) {
        final Long value = this.cache.getIfPresent(t);
        return value != null ? System.currentTimeMillis() - value : -1;
    }

    public boolean isCooldown(T t) {
        return this.cache.getIfPresent(t) != null;
    }

    public void removeCooldown(T t) {
        this.cache.invalidate(t);
    }

}
