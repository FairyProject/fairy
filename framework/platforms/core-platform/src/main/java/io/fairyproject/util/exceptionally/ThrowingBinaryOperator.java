package io.fairyproject.util.exceptionally;

import java.util.function.BinaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * Represents an operation upon two operands of the same type, producing a result
 * of the same type as the operands.  This is a specialization of
 * {@link io.fairyproject.util.exceptionally.ThrowingBiFunction} for the case where the operands and the result are all of
 * the same type.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <T> the type of the operands and result of the operator
 * @param <E> the type of the thrown checked exception
 *
 * @author Grzegorz Piwowarek
 * @see io.fairyproject.util.exceptionally.ThrowingBiFunction
 * @see io.fairyproject.util.exceptionally.ThrowingUnaryOperator
 */
public interface ThrowingBinaryOperator<T, E extends Exception> extends io.fairyproject.util.exceptionally.ThrowingBiFunction<T, T, T, E> {

    static <T> BinaryOperator<T> unchecked(ThrowingBinaryOperator<T, ?> function) {
        requireNonNull(function);
        return (arg1, arg2) -> {
            try {
                return function.apply(arg1, arg2);
            } catch (final Exception e) {
                throw new io.fairyproject.util.exceptionally.CheckedException(e);
            }
        };
    }
}
