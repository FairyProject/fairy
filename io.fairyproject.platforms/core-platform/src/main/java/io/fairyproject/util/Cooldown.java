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

package io.fairyproject.util;

import com.google.common.cache.RemovalCause;
import io.fairyproject.task.Task;
import io.fairyproject.util.terminable.Terminable;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * The Simple Cooldown Utility for Fairy
 */
@Getter
public class Cooldown<T> implements Terminable {

    private final Map<T, Long> cache;
    private final long defaultCooldown;
    private final Terminable task;
    private BiConsumer<T, RemovalCause> removalListener;

    public Cooldown(long defaultCooldown) {
        this(defaultCooldown, null);
    }

    public Cooldown(long defaultCooldown, BiConsumer<T, RemovalCause> removalListener) {
        this.removalListener = removalListener;
        this.cache = new ConcurrentHashMap<>();

        this.defaultCooldown = defaultCooldown;
        this.task = Task.asyncRepeated(t -> {
            long time = System.currentTimeMillis();
            for (Map.Entry<T, Long> entry : this.cache.entrySet()) {
                if (time >= entry.getValue()) {
                    this.cache.remove(entry.getKey());
                }
            }
        }, 1L);
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
        final Long value = this.cache.get(t);
        return value != null ? System.currentTimeMillis() - value : -1;
    }

    public boolean isCooldown(T t) {
        return this.cache.get(t) != null;
    }

    public void removeCooldown(T t) {
        this.cache.remove(t);
    }

    @Override
    public void close() throws Exception {
        this.task.close();
    }

    @Override
    public boolean isClosed() {
        return this.task.isClosed();
    }
}
