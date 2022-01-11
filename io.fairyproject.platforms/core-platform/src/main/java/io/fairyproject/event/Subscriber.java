package io.fairyproject.event;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public abstract class Subscriber<E> implements Comparable<Subscriber<E>> {

    private static final Logger LOGGER = LogManager.getLogger(Subscriber.class);

    private final Class<E> type;
    private final int priority;

    public Subscriber(Class<E> type, int priority) {
        this.type = type;
        this.priority = priority;
    }

    public boolean isInstance(Object obj) {
        return false;
    }

    public abstract void invoke(E event) throws Throwable;

    public void handleException(E event, Throwable throwable) {
        LOGGER.error("An exception has occurs while handing event " + event.getClass(), throwable);
    }

    protected final void invoke0(Object event) throws Throwable {
        this.invoke(this.type.cast(event));
    }

    protected final void handleException0(Object event, Throwable throwable) {
        this.handleException(this.type.cast(event), throwable);
    }

    @Override
    public int compareTo(@NotNull Subscriber<E> o) {
        return o.priority - this.priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscriber<?> that = (Subscriber<?>) o;
        return priority == that.priority && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, priority);
    }
}
