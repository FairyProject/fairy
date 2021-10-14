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

package io.fairyproject.bukkit.storage;

import io.fairyproject.Repository;

import java.util.UUID;

public abstract class ThreadedPlayerStorageConfigurationRepository<T> implements ThreadedPlayerStorageConfiguration<T> {
    @Override
    public final String getName() {
        return this.getRepository().getRepoId();
    }

    @Override
    public final T loadAsync(UUID uuid, String name) {
        return this.getRepository().findById(uuid).orElseGet(() -> this.create(uuid, name));
    }

    @Override
    public final void saveAsync(UUID uuid, T t) {
        this.getRepository().save(t);
    }

    public abstract T create(UUID uuid, String name);

    public abstract Repository<T, UUID> getRepository();
}
