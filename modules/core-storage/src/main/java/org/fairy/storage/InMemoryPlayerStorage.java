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

package org.fairy.storage;

import com.google.common.collect.Lists;
import org.fairy.bean.PostInitialize;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InMemoryPlayerStorage<T> implements PlayerStorage<T> {

    private Map<UUID, T> storedObjects;

    public abstract T create(UUID uuid);

    @PostInitialize
    public void onPostInitialize() {
        this.storedObjects = new ConcurrentHashMap<>();
    }

    @Override
    public T find(UUID uuid) {
        return this.storedObjects.computeIfAbsent(uuid, this::create);
    }

    @Override
    public CompletableFuture<T> save(UUID uuid) {
        return this.save(uuid, this.find(uuid));
    }

    @Override
    public CompletableFuture<T> save(UUID uuid, T t) {
        return CompletableFuture.completedFuture(t);
    }

    @Override
    public DataClosable<T> findAndSave(UUID uuid) {
        return new DataClosable<>(this, uuid, this.find(uuid));
    }

    @Override
    public List<T> findAll() {
        return Lists.newArrayList(this.storedObjects.values());
    }

    @Override
    public void unload(UUID uuid) {
        this.storedObjects.remove(uuid);
    }
}
