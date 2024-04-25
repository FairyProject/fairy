package io.fairyproject.util.exceptionally;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts one argument and returns a value;
 * Function might throw a checked exception instance.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of the thrown checked exception
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T arg) throws E;

    default Function<T, R> unchecked() {
        return unchecked(this);
    }

    /**
     * @return a Function that returns the result of the given function as an Optional instance.
     * In case of a failure, empty Optional is returned
     */
    static <T, R> Function<T, Optional<R>> lifted(final ThrowingFunction<? super T, ? extends R, ?> function) {
        requireNonNull(function);

        return t -> {
            try {
                return Optional.ofNullable(function.apply(t));
            } catch (final Exception e) {
                return Optional.empty();
            }
        };
    }

    static <T, R> Function<T, R> unchecked(final ThrowingFunction<? super T, ? extends R, ?> function) {
        requireNonNull(function);
        return t -> {
            try {
                return function.apply(t);
            } catch (final Exception e) {
                throw new io.fairyproject.util.exceptionally.CheckedException(e);
            }
        };
    }

    static <T1, R> Function<T1, R> sneaky(ThrowingFunction<? super T1, ? extends R, ?> function) {
        requireNonNull(function);
        return t -> {
            try {
                return function.apply(t);
            } catch (final Exception ex) {
                return SneakyThrowUtil.sneakyThrow(ex);
            }
        };
    }

    default Function<T, Optional<R>> lift() {
        return t -> {
            try {
                return Optional.ofNullable(apply(t));
            } catch (final Exception e) {
                return Optional.empty();
            }
        };
    }
}
