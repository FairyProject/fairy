/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.container.object.singleton;

import io.fairyproject.container.object.LifeCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SingletonObjectRegistryImplTest {

    private SingletonObjectRegistryImpl registry;

    @BeforeEach
    void setUp() {
        this.registry = new SingletonObjectRegistryImpl();
    }

    @Test
    public void testRegisterSingleton() {
        this.registry.registerSingleton(TestObject.class, new TestObject());

        assertTrue(this.registry.containsSingleton(TestObject.class));
    }

    @Test
    public void registerSingletonTwiceShouldThrowException() {
        this.registry.registerSingleton(TestObject.class, new TestObject());

        assertThrows(IllegalStateException.class, () -> this.registry.registerSingleton(TestObject.class, new TestObject()));
    }

    @Test
    public void testGetSingleton() {
        TestObject testObject = new TestObject();
        this.registry.registerSingleton(TestObject.class, testObject);

        assertEquals(testObject, this.registry.getSingleton(TestObject.class));
    }

    @Test
    public void testRemoveSingleton() {
        TestObject testObject = new TestObject();
        this.registry.registerSingleton(TestObject.class, testObject);

        this.registry.removeSingleton(TestObject.class);

        assertFalse(this.registry.containsSingleton(TestObject.class));
    }

    @Test
    public void testGetSingletonTypes() {
        TestObject testObject = new TestObject();
        this.registry.registerSingleton(TestObject.class, testObject);

        Set<Class<?>> singletonTypes = this.registry.getSingletonTypes();
        assertEquals(1, singletonTypes.size());
        assertTrue(singletonTypes.contains(TestObject.class));
    }

    @Nested
    class TestLifeCycle {

        @Test
        public void testGetSingletonLifeCycle() {
            registry.setSingletonLifeCycle(TestObject.class, LifeCycle.POST_INIT);

            assertEquals(LifeCycle.POST_INIT, registry.getSingletonLifeCycle(TestObject.class));
        }

        @Test
        public void testGetSingletonLifeCycleWhenNotSet() {
            assertEquals(LifeCycle.NONE, registry.getSingletonLifeCycle(TestObject.class));
        }

        @Test
        public void testRemoveSingletonShouldRemoveLifeCycle() {
            registry.setSingletonLifeCycle(TestObject.class, LifeCycle.POST_INIT);
            registry.removeSingleton(TestObject.class);

            assertEquals(LifeCycle.NONE, registry.getSingletonLifeCycle(TestObject.class));
        }

    }

    private static class TestObject {

    }

}