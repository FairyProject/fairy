package io.fairyproject.state.trigger;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class TriggerImpl<T> implements Trigger<T> {

    private final T t;
    private final boolean ended;

    public TriggerImpl(T t, boolean ended) {
        this.t = t;
        this.ended = ended;
    }

    @Override
    public @Nullable T get() {
        return this.t;
    }

    @Override
    public boolean ended() {
        return this.ended;
    }
}
