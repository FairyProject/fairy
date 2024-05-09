package io.fairyproject.util.exceptionally;

import java.util.Optional;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of {@link ThrowingFunction}.
 * Function may throw a checked exception.
 *
 * @param <T1> the type of the first argument to the function
 * @param <T2> the type of the second argument to the function
 * @param <R>  the type of the result of the function
 * @param <E>  the type of the thrown checked exception
 *
 * @author Grzegorz Piwowarek
 * @see ThrowingFunction
 */
@FunctionalInterface
public interface ThrowingBiFunction<T1, T2, R, E extends Exception> {
    R apply(T1 arg1, T2 arg2) throws E;

    static <T1, T2, R> BiFunction<T1, T2, R> unchecked(ThrowingBiFunction<? super T1, ? super T2, ? extends R, ?> function) {
        requireNonNull(function);

        return (arg1, arg2) -> {
            try {
                return function.apply(arg1, arg2);
            } catch (final Exception e) {
                throw new io.fairyproject.util.exceptionally.CheckedException(e);
            }
        };
    }

    static <T1, T2, R> BiFunction<T1, T2, R> sneaky(ThrowingBiFunction<? super T1, ? super T2, ? extends R, ?> function) {
        requireNonNull(function);
        return (t1, t2) -> {
            try {
                return function.apply(t1, t2);
            } catch (final Exception ex) {
                return SneakyThrowUtil.sneakyThrow(ex);
            }
        };
    }

    static <T1, T2, R> BiFunction<T1, T2, Optional<R>> optional(ThrowingBiFunction<? super T1, ? super T2, ? extends R, ?> function) {
        requireNonNull(function);

        return (arg1, arg2) -> {
            try {
                return Optional.ofNullable(function.apply(arg1, arg2));
            } catch (final Exception e) {
                return Optional.empty();
            }
        };
    }
}
