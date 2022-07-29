package io.fairyproject.event;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Subscribers {

    private final List<Subscriber<?>> subscribers;
    private final Class<?> type;
    private final List<Subscribers> parents;
    private final ReentrantReadWriteLock lock;
    @Nullable private Subscriber<?>[] bakedSubscribers;
    @Nullable private Subscribers child;

    public Subscribers(Class<?> type) {
        this.type = type;
        this.subscribers = new ArrayList<>(); // higher to lower
        this.parents = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public Subscribers(Class<?> type, @Nullable Subscribers child) {
        this(type);
        this.child = child;
        if (this.child != null) {
            this.child.parents.add(this);
        }
    }

    public boolean isEmpty() {
        this.lock.readLock().lock();
        boolean retVal;
        try {
            retVal = this.subscribers.isEmpty();
        } finally {
            this.lock.readLock().unlock();
        }
        return retVal;
    }

    public Subscriber<?>[] all() {
        Subscriber<?>[] subscribers;
        while ((subscribers = this.bakedSubscribers) == null) bake();
        return subscribers;
    }

    public void register(Subscriber<?> subscriber) {
        this.lock.writeLock().lock();
        try {
            Preconditions.checkArgument(!this.subscribers.contains(subscriber), "The subscriber has already been registered.");
            Preconditions.checkArgument(this.type.isAssignableFrom(subscriber.getType()), "The subscriber doesn't match the required event type.");
            this.subscribers.add(subscriber);
            this.subscribers.sort(Comparator.comparing(t -> t));
            this.removeBake();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void unregister(Subscriber<?> subscriber) {
        this.lock.writeLock().lock();
        try {
            if (this.subscribers.remove(subscriber)) {
                this.removeBake();
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void unregisterObject(Object obj) {
        this.lock.writeLock().lock();
        try {
            final Iterator<Subscriber<?>> iterator = this.subscribers.iterator();
            boolean changed = false;
            while (iterator.hasNext()) {
                final Subscriber<?> subscriber = iterator.next();
                if (subscriber.isInstance(obj)) {
                    iterator.remove();
                    changed = true;
                }
            }
            if (changed) {
                this.removeBake();
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void disband() {
        if (this.child != null) {
            this.child.parents.remove(this);
            this.child = null;
        }
        for (Subscribers parent : this.parents) {
            parent.child = null;
            parent.removeBake();
        }
    }

    public void clear() {
        this.lock.writeLock().lock();
        try {
            this.subscribers.clear();
            this.removeBake();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private Collection<Subscriber<?>> allInternal() {
        Collection<Subscriber<?>> subscribers;
        this.lock.readLock().lock();
        try {
            if (this.child != null) {
                subscribers = Stream.of(this.subscribers, this.child.subscribers)
                        .flatMap(List::stream)
                        .sorted()
                        .collect(Collectors.toList());
            } else {
                subscribers = this.subscribers;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return subscribers;
    }

    public void bake() {
        if (bakedSubscribers != null) {
            return;
        }
        bakedSubscribers = this.allInternal().toArray(new Subscriber[0]);
    }

    public void removeBake() {
        this.bakedSubscribers = null;
        for (Subscribers parent : this.parents) {
            parent.removeBake();
        }
    }

}
