package io.fairyproject.event;

public interface ISubscribers {

    boolean isEmpty();

    Subscriber<?>[] all();

    void register(Subscriber<?> subscriber);

    void unregister(Subscriber<?> subscriber);

}
