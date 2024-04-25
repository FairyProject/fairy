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

package io.fairyproject.container;

import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.scanner.ContainerNodeClassScanner;
import io.fairyproject.util.entry.Entry;
import io.fairytest.container.components.AccessPrototypeClass;
import io.fairytest.container.components.PrototypeClass;
import io.fairytest.container.components.SingletonClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerIntegrationTest {

    private ContainerContext context;

    @BeforeEach
    public void setUp() {
        this.context = new ContainerContext();
    }

    @Nested
    class EntireLifeCycle {

        private ContainerNode node;

        @BeforeEach
        public void setUp() {
            this.node = createContainerNode();

            context.loadContainerNode(this.node);
        }

        @Nested
        class Load {

            @Test
            public void checkAllComponentsAreLoaded() {
                assertEquals(4, node.all().size());
            }

            @Test
            public void checkStaticFieldIsInjected() {
                final SingletonClass singletonClass = SingletonClass.STATIC_WIRED;
                assertNotNull(singletonClass);
            }

            @Nested
            class Prototype {

                private Thread mainThread;
                private AccessPrototypeClass accessPrototypeClass;

                @BeforeEach
                public void setUp() {
                    this.mainThread = Thread.currentThread();
                    this.accessPrototypeClass = (AccessPrototypeClass) context.singletonObjectRegistry().getSingleton(AccessPrototypeClass.class);
                }

                @Test
                public void checkGeneratedTwoDifferentPrototypes() {
                    assertNotSame(accessPrototypeClass.getA(), accessPrototypeClass.getB());
                }

                @Test
                public void checkLifeCycleAnnotationIsCalledInEachPrototype() {
                    checkLifeCycleAnnotationIsCalled(accessPrototypeClass.getA());
                    checkLifeCycleAnnotationIsCalled(accessPrototypeClass.getB());
                }

                @Test
                public void checkPrototypeIsProperlyInjected() {
                    SingletonClass singleton = (SingletonClass) context.singletonObjectRegistry().getSingleton(SingletonClass.class);

                    assertEquals(accessPrototypeClass.getA().getSingleton(), singleton);
                    assertEquals(accessPrototypeClass.getB().getSingleton(), singleton);

                    assertEquals(accessPrototypeClass.getA().getSingletonAutowired(), singleton);
                    assertEquals(accessPrototypeClass.getB().getSingletonAutowired(), singleton);
                }

                /**
                 * Basically, after the nodes are loaded, the prototype objects should not be held by any strong references within Fairy.
                 * That's why I coded this test, to make sure that the prototype objects are not held by any strong references.
                 * But, I'm not sure if this is needed at all, it's currently quite stable but is it safe to test garbage collection?
                 */
                @Test
                @Disabled
                public void checkIfPrototypeCanBeGarbageCollected() {
                    WeakReference<PrototypeClass> prototypeWeakReference = new WeakReference<>(accessPrototypeClass.getA());
                    accessPrototypeClass.clear();
                    System.gc();

                    assertNull(prototypeWeakReference.get());
                }

                private void checkLifeCycleAnnotationIsCalled(PrototypeClass prototype) {
                    assertNotEquals(-1, prototype.getConstructTime());
                    assertNotEquals(-1, prototype.getPreInitTime());
                    assertNotEquals(-1, prototype.getPostInitTime());

                    assertTrue(prototype.getConstructTime() < prototype.getPreInitTime());
                    assertTrue(prototype.getPreInitTime() < prototype.getPostInitTime());

                    assertEquals(mainThread, prototype.getMainThreadConstruct());
                    assertEquals(mainThread, prototype.getMainThreadPreInit());
                    assertEquals(mainThread, prototype.getMainThreadPostInit());
                }

            }

            @Nested
            class Singleton {

                private Thread mainThread;
                private SingletonClass singletonClass;

                @BeforeEach
                public void setUp() {
                    this.mainThread = Thread.currentThread();
                    this.singletonClass = (SingletonClass) context.singletonObjectRegistry().getSingleton(SingletonClass.class);
                }

                @Test
                public void checkNotNull() {
                    assertNotNull(singletonClass);
                }

                @Test
                public void checkLifeCycleAnnotationIsCalled() {
                    assertNotEquals(-1, singletonClass.getConstructMs());
                    assertNotEquals(-1, singletonClass.getPreInitializeMs());
                    assertNotEquals(-1, singletonClass.getPostInitializeMs());

                    LifeCycle[] lifeCycleOrder = Stream.of(
                                    new Entry<>(LifeCycle.CONSTRUCT, singletonClass.getConstructMs()),
                                    new Entry<>(LifeCycle.PRE_INITIALIZE, singletonClass.getPreInitializeMs()),
                                    new Entry<>(LifeCycle.POST_INITIALIZE, singletonClass.getPostInitializeMs())
                            )
                            .sorted(Comparator.comparing(Entry::getValue))
                            .map(Entry::getKey)
                            .toArray(LifeCycle[]::new);

                    assertArrayEquals(new LifeCycle[]{
                            LifeCycle.CONSTRUCT,
                            LifeCycle.PRE_INITIALIZE,
                            LifeCycle.POST_INITIALIZE
                    }, lifeCycleOrder);
                }

                @Test
                public void checkLifeCycleAreCalledInCorrectThread() {
                    assertEquals(mainThread, singletonClass.getConstructThread());
                    assertEquals(mainThread, singletonClass.getPreInitializeThread());
                    assertEquals(mainThread, singletonClass.getPostInitializeThread());
                }

                @Test
                public void checkDestroyLifeCycleAreNotCalled() {
                    assertEquals(-1, singletonClass.getPreDestroyMs());
                    assertEquals(-1, singletonClass.getPostDestroyMs());
                }

            }

        }

        @Nested
        class Destroy {

            private SingletonClass singletonClass;

            @BeforeEach
            public void setUp() {
                this.singletonClass = (SingletonClass) context.singletonObjectRegistry().getSingleton(SingletonClass.class);
                context.nodeDestroyer().destroy(node);
            }

            @Test
            public void checkContainerObjNoLongerBound() {
                assertFalse(context.containerObjectBinder().isBound(SingletonClass.class));
                assertNull(context.containerObjectBinder().getBinding(SingletonClass.class));
            }

            @Nested
            class Singleton {

                private Thread mainThread;

                @BeforeEach
                public void setUp() {
                    this.mainThread = Thread.currentThread();
                }

                @Test
                public void checkLifeCycleAnnotationIsCalled() {
                    assertNotEquals(-1, singletonClass.getPreDestroyMs());
                    assertNotEquals(-1, singletonClass.getPostDestroyMs());

                    LifeCycle[] lifeCycleOrder = Stream.of(
                                    new Entry<>(LifeCycle.PRE_DESTROY, singletonClass.getPreDestroyMs()),
                                    new Entry<>(LifeCycle.POST_DESTROY, singletonClass.getPostDestroyMs())
                            )
                            .sorted(Comparator.comparing(Entry::getValue))
                            .map(Entry::getKey)
                            .toArray(LifeCycle[]::new);

                    assertArrayEquals(lifeCycleOrder, new LifeCycle[]{
                            LifeCycle.PRE_DESTROY,
                            LifeCycle.POST_DESTROY
                    });
                }

                @Test
                public void checkLifeCycleAreCalledInCorrectThread() {
                    assertEquals(mainThread, singletonClass.getPreDestroyThread());
                    assertEquals(mainThread, singletonClass.getPostDestroyThread());
                }

                @Test
                public void checkSingletonNoLongerAvailable() {
                    assertFalse(context.singletonObjectRegistry().containsSingleton(SingletonClass.class));
                    assertNull(context.singletonObjectRegistry().getSingleton(SingletonClass.class));
                }

            }

        }

    }

    private ContainerNode createContainerNode() {
        ContainerNode node = ContainerNode.create("test", context.containerObjectBinder());
        createClassScanner(node);
        return node;
    }

    private void createClassScanner(ContainerNode node) {
        ContainerNodeClassScanner classScanner = new ContainerNodeClassScanner(context, context.containerObjectBinder(), "test", node);
        classScanner.getClassLoaders().add(ContainerIntegrationTest.class.getClassLoader());
        classScanner.getUrls().add(ContainerIntegrationTest.class.getProtectionDomain().getCodeSource().getLocation());
        classScanner.getClassPaths().add("io.fairytest.container.components");
        classScanner.scan();
    }

    private enum LifeCycle {

        CONSTRUCT,
        PRE_INITIALIZE,
        POST_INITIALIZE,
        PRE_DESTROY,
        POST_DESTROY

    }

}
