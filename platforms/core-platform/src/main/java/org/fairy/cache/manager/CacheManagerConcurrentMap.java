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

package org.fairy.cache.manager;

import com.google.common.collect.ImmutableMap;
import org.aspectj.lang.JoinPoint;
import org.fairy.cache.CacheWrapper;
import org.fairy.cache.impl.CacheKeyAbstract;
import org.fairy.cache.CacheableAspect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheManagerConcurrentMap implements CacheManager {

    private transient ConcurrentMap<CacheKeyAbstract, CacheWrapper<?>> cache;

    private CacheableAspect cacheableAspect;

    @Override
    public void init(CacheableAspect cacheableAspect) {
        this.cacheableAspect = cacheableAspect;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void clean() {
        this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @Override
    public Map<CacheKeyAbstract, CacheWrapper<?>> getAsMap() {
        return ImmutableMap.copyOf(this.cache);
    }

    @Override
    public <T> Collection<T> findByType(Class<T> type) {
        Set<T> results = new HashSet<>();
        for (CacheWrapper<?> wrapper : this.cache.values()) {
            Object object = wrapper.get();
            if (type.isInstance(object)) {
                results.add((T) object);
            }
        }

        return results;
    }

    @Override
    public CacheWrapper<?> find(CacheKeyAbstract key) {
        CacheWrapper<?> wrapper = this.cache.get(key);
        if (wrapper != null && wrapper.isExpired()) {
            this.cache.remove(key);
            return null;
        }

        return wrapper;
    }

    @Override
    public void cache(CacheKeyAbstract key, CacheWrapper<?> wrapper) throws Throwable {
        this.cache.put(key, wrapper);
    }

    @Override
    public void evict(JoinPoint point, String keyString) {
        for (final CacheKeyAbstract key : this.cache.keySet()) {
            if (!key.equals(this.cacheableAspect.toKey(point, keyString))) {
                continue;
            }
            this.cache.remove(key);
        }
    }

    @Override
    public void flush(Class<?> parentClass) {
        for (final CacheKeyAbstract key : this.cache.keySet()) {
            if (parentClass != null && !parentClass.isAssignableFrom(key.getParentClass())) {
                continue;
            }
            this.cache.remove(key);
        }
    }

}
