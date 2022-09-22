package io.fairyproject.state.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TriggerKeyed implements Trigger<Object> {

    private final String name;

    private TriggerKeyed(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @Nullable Object get() {
        return null;
    }

    @Override
    public boolean ended() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggerKeyed that = (TriggerKeyed) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static <T> Trigger<T> of(String key) {
        return (Trigger<T>) new TriggerKeyed(key);
    }
}
