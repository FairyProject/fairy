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

package io.fairyproject.container.binder;

import io.fairyproject.container.object.ContainerObj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerObjectBinderImplTest {

    private ContainerObjectBinderImpl binder;

    @BeforeEach
    void setUp() {
        binder = new ContainerObjectBinderImpl();
    }

    @Test
    void getBinding_WhenNotBound_ShouldReturnNull() {
        Class<?> type = SomeClass.class;
        assertNull(binder.getBinding(type));
    }

    @Test
    void isBound_WhenNotBound_ShouldReturnFalse() {
        Class<?> type = SomeClass.class;
        assertFalse(binder.isBound(type));
    }

    @Test
    void bind_WhenBound_ShouldReturnBoundObject() {
        Class<?> type = SomeClass.class;
        ContainerObj object = ContainerObj.create(type);

        binder.bind(type, object);

        assertSame(object, binder.getBinding(type));
        assertTrue(binder.isBound(type));
    }

    @Test
    void unbind_ShouldRemoveBinding() {
        Class<?> type = SomeClass.class;
        ContainerObj object = ContainerObj.create(type);

        binder.bind(type, object);
        assertTrue(binder.isBound(type));

        binder.unbind(type);
        assertFalse(binder.isBound(type));
        assertNull(binder.getBinding(type));
    }

    @Test
    void getBinding_ShouldReturnBoundObjectWithSameInterface() {
        Class<?> type = SomeClass.class;
        Class<?> interfaceType = SomeInterface.class;
        ContainerObj object = ContainerObj.create(type);

        binder.bind(type, object);

        assertSame(object, binder.getBinding(interfaceType));
        assertTrue(binder.isBound(interfaceType));
    }


    @Test
    void getExactBinding_ShouldReturnExactTheClassThatRequested() {
        Class<?> classType = SomeClass.class;
        Class<?> interfaceType = SomeInterface.class;

        ContainerObj classTypeObj = ContainerObj.create(classType);

        binder.bind(classType, classTypeObj);

        assertNull(binder.getExactBinding(interfaceType));
    }

    @Test
    void recreateBinder_ShouldHaveDifferentContent() {
        Class<?> type = SomeClass.class;
        ContainerObj object = ContainerObj.create(type);

        binder.bind(type, object);
        assertTrue(binder.isBound(type));

        ContainerObjectBinderImpl newBinder = new ContainerObjectBinderImpl();
        assertFalse(newBinder.isBound(type));
        assertNull(newBinder.getBinding(type));
    }

    private static class SomeClass implements SomeInterface {
        // Define a nested class for testing
    }

    private interface SomeInterface {
        // Define a nested interface for testing
    }
}