package io.fairyproject.util.exceptionally;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents an action that can be performed.
 * Function might throw a checked exception instance.
 *
 * @param <E> the type of the thrown checked exception
 *
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    void run() throws E;

    static Runnable unchecked(ThrowingRunnable<?> runnable) {
        requireNonNull(runnable);
        return () -> {
            try {
                runnable.run();
            } catch (final Exception e) {
                throw new io.fairyproject.util.exceptionally.CheckedException(e);
            }
        };
    }

    /**
     * Returns a new Runnable instance which rethrows the checked exception using the Sneaky Throws pattern
     *
     * @return Runnable instance that rethrows the checked exception using the Sneaky Throws pattern
     */
    static Runnable sneaky(ThrowingRunnable<?> runnable) {
        Objects.requireNonNull(runnable);
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                SneakyThrowUtil.sneakyThrow(e);
            }
        };
    }
}
