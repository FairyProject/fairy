package io.fairyproject.util.exceptionally;

import java.util.Objects;
import java.util.function.BiPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a predicate (boolean-valued function) of two arguments.  This is
 * the two-arity specialization of {@link io.fairyproject.util.exceptionally.ThrowingPredicate}.
 * Function may throw a checked exception.
 *
 * @param <T> the type of the first argument to the predicate
 * @param <U> the type of the second argument to the predicate
 * @param <E> the type of the thrown checked exception
 *
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingBiPredicate<T, U, E extends Exception> {
    boolean test(T t, U u) throws E;

    static <T, U> BiPredicate<T, U> unchecked(ThrowingBiPredicate<? super T, ? super U, ?> predicate) {
        requireNonNull(predicate);
        return (arg1, arg2) -> {
            try {
                return predicate.test(arg1, arg2);
            } catch (final Exception e) {
                throw new io.fairyproject.util.exceptionally.CheckedException(e);
            }
        };
    }

    /**
     * @return BiPredicate instance that rethrows the checked exception using the Sneaky Throws pattern
     */
    static <T, U> BiPredicate<T, U> sneaky(ThrowingBiPredicate<? super T, ? super U, ?> predicate) {
        Objects.requireNonNull(predicate);
        return (t, u) -> {
            try {
                return predicate.test(t, u);
            } catch (Exception e) {
                return SneakyThrowUtil.sneakyThrow(e);
            }
        };
    }
}
