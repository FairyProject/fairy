package io.fairyproject.event;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Subscribers {

    private final TreeSet<Subscriber<?>> subscribers;
    private final Class<?> type;
    private final List<Subscribers> parents;
    @Nullable private Subscriber<?>[] bakedSubscribers;
    @Nullable private Subscribers child;

    public Subscribers(Class<?> type) {
        this.type = type;
        this.subscribers = new TreeSet<>(); // higher to lower
        this.parents = new ArrayList<>();
    }

    public Subscribers(Class<?> type, @Nullable Subscribers child) {
        this(type);
        this.child = child;
        if (this.child != null) {
            this.child.parents.add(this);
        }
    }

    public boolean isEmpty() {
        return this.subscribers.isEmpty();
    }

    public Subscriber<?>[] all() {
        Subscriber<?>[] subscribers;
        while ((subscribers = this.bakedSubscribers) == null) bake();
        return subscribers;
    }

    public void register(Subscriber<?> subscriber) {
        synchronized (this) {
            Preconditions.checkArgument(!this.subscribers.contains(subscriber), "The subscriber has already been registered.");
            Preconditions.checkArgument(this.type.isAssignableFrom(subscriber.getType()), "The subscriber doesn't match the required event type.");
            this.subscribers.add(subscriber);
            this.removeBake();
        }
    }

    public void unregister(Subscriber<?> subscriber) {
        if (this.subscribers.remove(subscriber)) {
            this.removeBake();
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

    private Collection<Subscriber<?>> allInternal() {
        if (this.child != null) {
            return Stream.of(this.subscribers, this.child.subscribers)
                    .flatMap(TreeSet::stream)
                    .sorted()
                    .collect(Collectors.toList());
        }
        return this.subscribers;
    }

    public void bake() {
        if (bakedSubscribers != null) {
            return;
        }
        synchronized (this) {
            bakedSubscribers = this.allInternal().toArray(new Subscriber[0]);
        }
    }

    public void removeBake() {
        synchronized (this) {
            this.bakedSubscribers = null;
        }
        for (Subscribers parent : this.parents) {
            parent.removeBake();
        }
    }

}
