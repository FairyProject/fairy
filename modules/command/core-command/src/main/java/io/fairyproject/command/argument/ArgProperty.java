package io.fairyproject.command.argument;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ArgProperty<T> {

    public static <T> ArgProperty<T> create(String key, Class<T> type) {
        return new ArgProperty<>(key, type);
    }

    private final String key;
    private final Class<T> type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgProperty<?> that = (ArgProperty<?>) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
