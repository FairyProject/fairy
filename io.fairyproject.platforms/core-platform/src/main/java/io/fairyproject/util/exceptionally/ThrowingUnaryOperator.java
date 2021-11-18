package io.fairyproject.util.exceptionally;

import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * Represents an operation on a single operand that produces a result of the
 * same type as its operand.  This is a specialization of {@code Function} for
 * the case where the operand and result are of the same type.
 * Function may throw a checked exception.
 *
 * @param <T> the type of the operand and result of the operator
 * @param <E> the type of the thrown checked exception
 *
 * @see ThrowingFunction
 *
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingUnaryOperator<T, E extends Exception> extends ThrowingFunction<T, T, E> {

    static <T> UnaryOperator<T> unchecked(ThrowingUnaryOperator<T, ?> operator) {
        requireNonNull(operator);
        return t -> {
            try {
                return operator.apply(t);
            } catch (final Exception e) {
                throw new CheckedException(e);
            }
        };
    }

    /**
     * Returns a new UnaryOperator instance which rethrows the checked exception using the Sneaky Throws pattern
     * @return UnaryOperator instance that rethrows the checked exception using the Sneaky Throws pattern
     */
    static <T> UnaryOperator<T> sneaky(ThrowingUnaryOperator<T, ?> operator) {
        requireNonNull(operator);
        return t -> {
            try {
                return operator.apply(t);
            } catch (Exception e) {
                return SneakyThrowUtil.sneakyThrow(e);
            }
        };
    }
}
