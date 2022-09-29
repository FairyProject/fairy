package io.fairyproject.state.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Trigger<T> {

    Trigger<?> START = new TriggerImpl<>(null, true);
    Trigger<?> END = new TriggerImpl<>(null, true);

    @NotNull static <T> Trigger<T> of(@NotNull T t) {
        return new TriggerImpl<>(t, false);
    }

    @NotNull static <T> Trigger<T> keyed(String name) {
        return TriggerKeyed.of(name);
    }

    @NotNull static <T> Trigger<T> start() {
        return (Trigger<T>) START;
    }

    @NotNull static <T> Trigger<T> end() {
        return (Trigger<T>) END;
    }

    @Nullable T get();

    boolean ended();

}
