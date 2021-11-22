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

package io.fairytest.aspect;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import io.fairyproject.CacheEvict;
import io.fairyproject.CachePut;
import io.fairyproject.Cacheable;
import io.fairyproject.cache.EnableOwnCacheManager;
import org.junit.Test;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class CacheableTest {

    private static final Random RANDOM = new SecureRandom();

    @Test
    public void cacheSimpleCall() {
        Foo foo = new Foo(1L);
        String first = foo.get().toString();
        MatcherAssert.assertThat(first, CoreMatchers.equalTo(foo.get().toString()));
        foo.flush();
        MatcherAssert.assertThat(
                foo.get().toString(),
                CoreMatchers.not(CoreMatchers.equalTo(first))
        );
    }

    @Test
    public void cachesSimpleStaticCall() throws Exception {
        final String first = Foo.staticGet();
        MatcherAssert.assertThat(
                first,
                CoreMatchers.equalTo(Foo.staticGet())
        );
        Foo.staticFlush();
        MatcherAssert.assertThat(
                Foo.staticGet(),
                CoreMatchers.not(CoreMatchers.equalTo(first))
        );
    }

    @Test
    public void flushAndEnsureNonNull() {
        Imanity imanity = new Imanity();
        for (int i = 0; i < 100000; i++) {
            final String hi = imanity.test("hi");
            Assert.notNull(hi, "The result is null!");
            imanity.clear();
        }
    }

    @Test
    public void evictAndEnsureNonNull() {
        Imanity imanity = new Imanity();
        for (int i = 0; i < 100000; i++) {
            final String hi = imanity.test("hi");
            Assert.notNull(hi, "The result is null!");
            imanity.evict("hi");
        }
    }

    @Test
    public void flushesWithStaticTrigger() throws Exception {
        final Bar bar = new Bar();
        MatcherAssert.assertThat(
                bar.get(),
                CoreMatchers.equalTo(bar.get())
        );
    }

    @Test
    public void testKey() throws Exception {
        Imanity imanity = new Imanity();

        int id = 2;
        long first = imanity.test(id);

        MatcherAssert.assertThat(first, CoreMatchers.equalTo(imanity.test(id)));
        long second = imanity.test(5);

        MatcherAssert.assertThat(first, CoreMatchers.not(second));

        imanity.evict(id);
        long third = imanity.test(id);

        MatcherAssert.assertThat(first, CoreMatchers.not(third));

        long dummyObjectA = imanity.test(new ImanityDummy(id));
        long dummyObjectB = imanity.test(new ImanityDummy(id));

        MatcherAssert.assertThat(dummyObjectA, CoreMatchers.equalTo(dummyObjectB));

        imanity.evict(id);

        MatcherAssert.assertThat(dummyObjectA, CoreMatchers.not(imanity.test(new ImanityDummy(id))));

        id = 3;
        long testPut = RANDOM.nextLong();

        imanity.put(id, testPut);
        MatcherAssert.assertThat(testPut, CoreMatchers.equalTo(testPut));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullParameter() throws Exception {
        Imanity imanity = new Imanity();

        imanity.test((String) null);
    }

    @EnableOwnCacheManager
    private static final class Imanity {

        @Cacheable(key = "'test-' + #args[0]")
        public long test(int id) {
            return RANDOM.nextLong();
        }

        @Cacheable(key = "'test-' + #args[0].getId()")
        public long test(ImanityDummy dummy) {
            return RANDOM.nextLong();
        }

        @Cacheable(key = "'test-' + #args[0]")
        public String test() {
            return "hi";
        }

        @Cacheable(key = "'test-' + #args[0]")
        public String test(String text) {
            return "lol";
        }

        @CacheEvict(value = "'test-' + #args[0]")
        public void evict(int id) {

        }

        @CacheEvict(value = "'test-' + #args[0]")
        public void evict(String id) {

        }

        @CachePut(value = "'test-' + #args[0]")
        public long put(int id, long value) {
            return value;
        }

        @Cacheable.ClearBefore
        public void clear() {

        }

    }

    public static final class ImanityDummy {

        public int getId() {
            return id;
        }

        private int id;

        public ImanityDummy(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImanityDummy that = (ImanityDummy) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ImanityDummy.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .toString();
        }
    }

    private static final class Foo {

        private final transient long number;

        Foo(final long num) {
            this.number = num;
        }

        @Override
        public int hashCode() {
            return this.get().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this;
        }

        @Override
        @Cacheable(forever = true)
        public String toString() {
            return Long.toString(this.number);
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public Foo get() {
            return new Foo(CacheableTest.RANDOM.nextLong());
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public Foo never() {
            try {
                TimeUnit.HOURS.sleep(1L);
            } catch (final InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
            return this;
        }

        @Cacheable.ClearBefore
        public void flush() {
            // nothing to do
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public static String staticGet() {
            return Long.toString(CacheableTest.RANDOM.nextLong());
        }

        @Cacheable.ClearBefore
        public static void staticFlush() {
            // nothing to do
        }
    }

    public static final class Bar {

        @Cacheable
        public long get() {
            return CacheableTest.RANDOM.nextLong();
        }

    }

}
