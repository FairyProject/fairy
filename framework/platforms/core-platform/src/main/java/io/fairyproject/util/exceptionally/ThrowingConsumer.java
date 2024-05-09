package io.fairyproject.util.exceptionally;

import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts one argument and does not return any value;
 * Function might throw a checked exception instance.
 *
 * @param <T> the type of the input to the function
 * @param <E> the type of the thrown checked exception
 *
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

    void accept(T t) throws E;

    static <T> Consumer<T> unchecked(ThrowingConsumer<? super T, ?> consumer) {
        requireNonNull(consumer);
        return t -> {
            try {
                consumer.accept(t);
            } catch (final Exception e) {
                throw new CheckedException(e);
            }
        };
    }

    /**
     * Returns a new BiConsumer instance which rethrows the checked exception using the Sneaky Throws pattern
     * @return BiConsumer instance that rethrows the checked exception using the Sneaky Throws pattern
     */
    static <T> Consumer<T> sneaky(ThrowingConsumer<? super T, ?> consumer) {
        Objects.requireNonNull(consumer);
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                SneakyThrowUtil.sneakyThrow(e);
            }
        };
    }
}
