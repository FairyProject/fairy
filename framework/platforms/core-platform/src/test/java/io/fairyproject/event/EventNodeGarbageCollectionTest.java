package io.fairyproject.event;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertNull;

public class EventNodeGarbageCollectionTest {

    static class EventTest implements Event {
    }

    @Test
    public void removedChildCanBeGarbageCollected() throws InterruptedException {
        EventNode<Event> parent = EventNode.all("parent");
        WeakReference<EventNode<Event>> child = attachCallAndRemoveChild(parent);
        // keep dispatching like a server would, so the parent rebuilds its consumer without the child
        parent.call(new EventTest());

        for (int i = 0; i < 50 && child.get() != null; i++) {
            System.gc();
            Thread.sleep(10);
        }
        assertNull(child.get(), "A removed child node should be garbage collectable");
    }

    private static WeakReference<EventNode<Event>> attachCallAndRemoveChild(EventNode<Event> parent) {
        EventNode<Event> child = EventNode.all("child");
        child.addListener(EventTest.class, event -> {
        });
        parent.addChild(child);
        // building the parent's consumer requests a handle from every child
        parent.call(new EventTest());
        parent.removeChild(child);
        return new WeakReference<>(child);
    }

}
