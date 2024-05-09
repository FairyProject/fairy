package io.fairyproject.util.exceptionally;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts zero arguments and returns some value.
 * Function might throw a checked exception instance.
 *
 * @param <T> the type of the output to the function
 * @param <E> the type of the thrown checked exception
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;

    static <T> Supplier<T> unchecked(ThrowingSupplier<? extends T, ?> supplier) {
        requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (final Exception e) {
                throw new CheckedException(e);
            }
        };
    }

    static <T> Supplier<Optional<T>> optional(ThrowingSupplier<? extends T, ?> supplier) {
        requireNonNull(supplier);
        return () -> {
            try {
                return Optional.ofNullable(supplier.get());
            } catch (final Exception e) {
                return Optional.empty();
            }
        };
    }

    /**
     * Returns a new Supplier instance which rethrows the checked exception using the Sneaky Throws pattern
     * @return Supplier instance that rethrows the checked exception using the Sneaky Throws pattern
     */
    static <T> Supplier<T> sneaky(ThrowingSupplier<? extends T, ?> supplier) {
        requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (final Exception ex) {
                return SneakyThrowUtil.sneakyThrow(ex);
            }
        };
    }
}
