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

package io.fairyproject.event;

import io.fairyproject.event.Cancellable;
import io.fairyproject.event.Event;
import io.fairyproject.event.EventListener;
import io.fairyproject.event.EventNode;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class EventNodeTest {

    static class EventTest implements Event {
    }

    static class CancellableTest implements Event, Cancellable {
        private boolean cancelled = false;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }
    }

    @Test
    public void testCall() {
        var node = EventNode.all("main");
        AtomicBoolean result = new AtomicBoolean(false);
        var listener = EventListener.of(EventTest.class, eventTest -> result.set(true));
        node.addListener(listener);
        assertFalse(result.get(), "The event should not be called before the call");
        node.call(new EventTest());
        assertTrue(result.get(), "The event should be called after the call");

        // Test removal
        result.set(false);
        node.removeListener(listener);
        node.call(new EventTest());
        assertFalse(result.get(), "The event should not be called after the removal");
    }

    @Test
    public void testHandle() {
        var node = EventNode.all("main");
        var handle = node.getHandle(EventTest.class);
        assertSame(handle, node.getHandle(EventTest.class));

        var handle1 = node.getHandle(CancellableTest.class);
        assertSame(handle1, node.getHandle(CancellableTest.class));
    }

    @Test
    public void testCancellable() {
        var node = EventNode.all("main");
        AtomicBoolean result = new AtomicBoolean(false);
        var listener = EventListener.builder(CancellableTest.class)
                .handler(event -> {
                    event.setCancelled(true);
                    result.set(true);
                    assertTrue(event.isCancelled(), "The event should be cancelled");
                }).build();
        node.addListener(listener);
        node.call(new CancellableTest());
        assertTrue(result.get(), "The event should be called after the call");

        // Test cancelling
        node.addListener(CancellableTest.class, event -> fail("The event must have been cancelled"));
        node.call(new CancellableTest());
    }

    @Test
    public void testChildren() {
        var node = EventNode.all("main");
        AtomicInteger result = new AtomicInteger(0);
        var child1 = EventNode.all("child1").setPriority(1)
                .addListener(EventTest.class, eventTest -> {
                    assertEquals(0, result.get(), "child1 should be called before child2");
                    result.set(1);
                });
        var child2 = EventNode.all("child2").setPriority(2)
                .addListener(EventTest.class, eventTest -> {
                    assertEquals(1, result.get(), "child2 should be called after child1");
                    result.set(2);
                });
        node.addChild(child1);
        node.addChild(child2);
        assertEquals(node.getChildren().size(), 2, "The node should have 2 children");
        node.call(new EventTest());
        assertEquals(2, result.get(), "The event should be called after the call");

        // Test removal
        result.set(0);
        node.removeChild(child2);
        assertEquals(node.getChildren().size(), 1, "The node should have 1 child");
        node.call(new EventTest());
        assertEquals(1, result.get(), "child2 should has been removed");

        result.set(0);
        node.removeChild(child1);
        node.call(new EventTest());
        assertTrue(node.getChildren().isEmpty(), "The node should have no child left");
        assertEquals(0, result.get(), "The event should not be called after the removal");
    }
}
