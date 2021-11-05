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

package io.fairyproject;

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractRepositoryProvider implements RepositoryProvider {

    private final String id;
    private final Set<Repository<?, ?>> repositories;
    private final ReentrantLock lock;

    public AbstractRepositoryProvider(String id) {
        this.id = id;
        this.repositories = Sets.newConcurrentHashSet();
        this.lock = new ReentrantLock();
    }

    @Override
    public final void build() {
        this.lock.lock();

        this.build0();
        this.repositories.forEach(Repository::init);

        this.lock.unlock();
    }

    public abstract void build0();

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public final <E, ID extends Serializable> Repository<E, ID> buildRepository(Class<E> entityType, String repoId) {
        final Repository<E, ID> repository = this.createRepository(entityType, repoId);
        repository.init();

        this.repositories.add(repository);
        return repository;
    }

    public abstract <E, ID extends Serializable> Repository<E, ID> createRepository(Class<E> entityType, String repoId);

    @Override
    public ReentrantLock getIOLock() {
        return this.lock;
    }
}
