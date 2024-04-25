package io.fairyproject.util.filter;

import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface FilterUnit<T> {

    static <T> FilterUnit<T> create() {
        return new FilterUnitImpl<>();
    }

    static <T, E extends Exception> Predicate<T> test(@NotNull ThrowingConsumer<T, E> consumer, @NotNull Class<E> exceptionClass) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                if (exceptionClass.isInstance(e))
                    return false;
                // unexpected error!!!
                SneakyThrowUtil.sneakyThrow(e);
            }
            return true;
        };
    }

    @Contract("_ -> this")
    FilterUnit<T> predicate(@NotNull Predicate<T> predicate);

    @Contract("_ -> this")
    FilterUnit<T> addAll(@NotNull Iterable<T> iterable);

    @Contract("_ -> this")
    FilterUnit<T> add(@Nullable T t);

    @Contract("_, _ -> this")
    FilterUnit<T> add(@Nullable T t, @NotNull Predicate<T> predicate);

    @Contract("_ -> this")
    FilterUnit<T> add(@NotNull Item<T> item);

    @NotNull Optional<T> find();

    @NotNull Stream<T> findAll();

    interface Item<T> {

        static <T> Item<T> create(@Nullable T value) {
            return new FilterUnitImpl.ItemImpl<>(value);
        }

        boolean match();

        @Nullable T get();

        @NotNull Iterable<Predicate<T>> predicates();

        @Contract("_ -> this")
        Item<T> predicate(@NotNull Predicate<T> predicate);

    }

}
