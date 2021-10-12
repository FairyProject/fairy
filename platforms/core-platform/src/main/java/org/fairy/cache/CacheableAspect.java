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

package org.fairy.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.fairy.CacheEvict;
import org.fairy.CachePut;
import org.fairy.Cacheable;
import org.fairy.cache.impl.CacheKeyAbstract;
import org.fairy.cache.impl.CacheKeyMethod;
import org.fairy.cache.impl.CacheKeyString;
import org.fairy.cache.manager.CacheManager;
import org.fairy.cache.manager.CacheManagerConcurrentMap;
import org.fairy.cache.script.AbstractScriptParser;
import org.fairy.util.Stacktrace;
import org.fairy.cache.script.SpringELParser;
import org.intellij.lang.annotations.Language;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 * A Modified version of AspectJ cacheable annotation
 *
 * Provide key, evict and cache managers
 *
 */
@Aspect
public class CacheableAspect {

    public static final Logger LOGGER = LogManager.getLogger(CacheableAspect.class);
    public static CacheableAspect INSTANCE;

    private transient final CacheManager defaultCacheManager;
    private transient final Map<Class<?>, CacheManager> cacheManagers;

    public static ScheduledExecutorService CLEANER_SERVICE;
    public static ExecutorService UPDATER_SERVICE;

    private final AbstractScriptParser scriptParser;

    public CacheableAspect() {
        INSTANCE = this;

        CLEANER_SERVICE = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("cacheable-clean")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
                .build()
        );
        UPDATER_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("cacheable-update")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
                .build()
        );

        this.defaultCacheManager = new CacheManagerConcurrentMap();
        this.defaultCacheManager.init(this)
        ;

        this.cacheManagers = new ConcurrentHashMap<>(0);
        this.scriptParser = new SpringELParser();

        CLEANER_SERVICE.scheduleAtFixedRate(() -> {
            this.defaultCacheManager.clean();

            for (CacheManager cacheManager : this.cacheManagers.values()) {
                cacheManager.clean();
            }
        }, 1L, 1L, TimeUnit.SECONDS);

    }

    // TODO: Performance check, is this key reader efficient?

    private String readAnnotationKey(JoinPoint point, String value, boolean preventNull) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        Object[] args = point.getArgs();

        if (preventNull) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    throw new IllegalArgumentException("The argument with index " + i + " in method " + point.getSignature().getName() + " is null!");
                }
            }
        }

        try {
            return this.scriptParser.getDefinedCacheKey(value, point.getTarget(), point.getArgs(), null, false);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return "";
    }

    public CacheManager getCacheManager(Class<?> type) {
        CacheManager cacheManager = this.cacheManagers.getOrDefault(type, null);

        if (cacheManager == null) {
            EnableOwnCacheManager annotation = type.getAnnotation(EnableOwnCacheManager.class);
            if (annotation != null) {
                try {
                    cacheManager = annotation.value().newInstance();
                } catch (Throwable throwable) {
                    throw new RuntimeException("Something wrong while creating Cache Manager instance for " + type.getName() + "!", throwable);
                }
                cacheManager.init(this);
                this.cacheManagers.put(type, cacheManager);
            }
        }

        return cacheManager == null ? this.defaultCacheManager : cacheManager;
    }

    public CacheKeyAbstract toKey(JoinPoint point, String key) {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();
        Class<?> parentClass = method.getDeclaringClass();

        if (key != null && !key.isEmpty()) {
            return new CacheKeyString(parentClass, key);
        }

        return new CacheKeyMethod(parentClass, point);
    }

    @SneakyThrows
    public boolean checkCondition(@Language("SpEL") String condition, Object target, Object[] arguments, Object retVal, boolean hasRetVal) {
        boolean result = true;
        if (arguments != null && arguments.length > 0 && condition != null && condition.length() > 0) {
            result = this.scriptParser.getElValue(condition, target, arguments, retVal, hasRetVal, Boolean.class);
        }
        return result;
    }

    @Around("execution(* *(..)) && @annotation(org.fairy.Cacheable)")
    public Object cache(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final Cacheable annotation = method.getAnnotation(Cacheable.class);
        CacheKeyAbstract key = this.toKey(point, readAnnotationKey(point, annotation.key(), annotation.preventArgumentNull()));
        @Language("SpEL") String condition = annotation.condition();

        CacheManager manager = this.getCacheManager(method.getDeclaringClass());
        CacheWrapper<?> wrapper = manager.find(key);

        if (wrapper != null) {
            return wrapper.get();
        }

        Object result = point.proceed();

        if (condition.length() != 0 && !this.checkCondition(condition, point.getTarget(), point.getArgs(), result, true)) {
            return result;
        }

        wrapper = new CacheWrapper<>(result, annotation.forever() ? 0L : annotation.unit().toMillis(annotation.lifetime()));
        manager.cache(key, wrapper);
        return result;
    }

    @Around("execution(* *(..)) && @annotation(org.fairy.CachePut)")
    public Object cachePut(ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final CachePut annotation = method.getAnnotation(CachePut.class);
        CacheKeyAbstract key = this.toKey(point, readAnnotationKey(point, annotation.value(), annotation.preventArgumentNull()));
        @Language("SpEL") String condition = annotation.condition();

        CacheManager manager = this.getCacheManager(method.getDeclaringClass());

        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("The method " + method.toString() + " marked @CachePut but return type is null!");
        }
        Object result = point.proceed();

        if (condition.length() != 0 && !this.checkCondition(condition, point.getTarget(), point.getArgs(), result, true)) {
            return result;
        }

        CacheWrapper<?> wrapper = new CacheWrapper<>(result, annotation.forever() ? 0L : annotation.unit().toMillis(annotation.lifetime()));
        manager.cache(key, wrapper);
        return result;
    }

    @Before(
           "execution(* *(..)) && @annotation(org.fairy.CacheEvict)"
    )
    public void evict(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        CacheEvict annotation = method.getAnnotation(CacheEvict.class);
        String keyString = this.readAnnotationKey(point, annotation.value(), annotation.preventArgumentNull());
        @Language("JavaScript") String condition = annotation.condition();

        if (condition.length() > 0) {
            boolean conditionResult = this.checkCondition(condition, point.getTarget(), point.getArgs(), null, false);

            if (!conditionResult) {
                return;
            }
        }
        this.getCacheManager(method.getDeclaringClass()).evict(point, keyString);
    }

    @Before
            (
                    // @checkstyle StringLiteralsConcatenation (3 lines)
                    "execution(* *(..))"
                            + " && @annotation(org.fairy.Cacheable.ClearBefore)"
            )
    public void preFlush(final JoinPoint point) {
        this.flush(point);
    }

    @After
            (
                    // @checkstyle StringLiteralsConcatenation (2 lines)
                    "execution(* *(..))"
                            + " && @annotation(org.fairy.Cacheable.ClearAfter)"
            )
    public void postFlush(final JoinPoint point) {
        this.flush(point);
    }

    private void flush(final JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        this.getCacheManager(method.getDeclaringClass()).flush(method.getDeclaringClass());
    }

}
