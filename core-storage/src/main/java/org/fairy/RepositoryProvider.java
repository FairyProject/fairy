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

package org.fairy;

import org.fairy.config.StorageConfiguration;
import org.fairy.providers.H2RepositoryProvider;
import org.fairy.providers.MongoRepositoryProvider;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public interface RepositoryProvider extends AutoCloseable {

    static RepositoryProvider create(RepositoryType type, Path parentFolder, String providerId) {
        switch (type) {
            case H2:
                return new H2RepositoryProvider(providerId, parentFolder);
            case MONGO:
                return new MongoRepositoryProvider(providerId);
        }

        throw new NullPointerException();
    }

    /**
     * get Id of the provider
     *
     * @return id
     */
    String id();

    /**
     * init Repository Provider
     */
    void build();

    /**
     * Build Repository for consumers
     *
     * @param <E> Repository Data Entity
     * @param <ID> Repository ID Key
     * @return Repository
     */
    <E, ID extends Serializable> Repository<E, ID> buildRepository(Class<E> entityType, String repoId);

    /**
     * get IO Lock for the repository
     *
     * @return the Repository
     */
    ReentrantLock getIOLock();

    Map<String, String> getDefaultOptions();

    default void verify(Map<String, String> map) {
        final Map<String, String> defaults = this.getDefaultOptions();

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            map.computeIfAbsent(entry.getKey(), ignored -> entry.getValue());
        }
        map.entrySet().removeIf(entry -> !defaults.containsKey(entry.getKey()));
    }

    void registerOptions(Map<String, String> map);

}
