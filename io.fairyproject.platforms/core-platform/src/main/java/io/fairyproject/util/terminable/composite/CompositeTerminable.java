/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.util.terminable.composite;

import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Terminable} made up of several other {@link Terminable}s.
 *
 * <p>The {@link #close()} method closes in LIFO (Last-In-First-Out) order.</p>
 *
 * <p>{@link Terminable}s can be reused. The instance is effectively
 * cleared on each invocation of {@link #close()}.</p>
 *
 * @author lucko
 */
public interface CompositeTerminable extends Terminable, TerminableConsumer {

    /**
     * Creates a new standalone {@link CompositeTerminable}.
     *
     * @return a new {@link CompositeTerminable}
     */
    @NotNull
    static CompositeTerminable create() {
        return new AbstractCompositeTerminable();
    }

    /**
     * Creates a new standalone {@link CompositeTerminable}, which wraps
     * contained terminables in {@link java.lang.ref.WeakReference}s.
     *
     * @return a new {@link CompositeTerminable}
     */
    @NotNull
    static CompositeTerminable createWeak() {
        return new AbstractWeakCompositeTerminable();
    }

    /**
     * Closes this composite terminable.
     *
     * @throws CompositeClosingException if any of the sub-terminables throw an
     *                                   exception on close
     */
    @Override
    void close() throws CompositeClosingException;

    @Nullable
    @Override
    default CompositeClosingException closeSilently() {
        try {
            close();
            return null;
        } catch (CompositeClosingException e) {
            return e;
        }
    }

    @Override
    default void closeAndReportException() {
        try {
            close();
        } catch (CompositeClosingException e) {
            e.printAllStackTraces();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Binds an {@link AutoCloseable} with this composite closable.
     *
     * <p>Note that implementations do not keep track of duplicate contained
     * terminables. If a single {@link AutoCloseable} is added twice, it will be
     * {@link AutoCloseable#close() closed} twice.</p>
     *
     * @param terminable the closable to bind
     * @throws NullPointerException if the closable is null
     * @return this (for chaining)
     */
    CompositeTerminable with(Terminable terminable);

    /**
     * Binds all given {@link AutoCloseable} with this composite closable.
     *
     * <p>Note that implementations do not keep track of duplicate contained
     * terminables. If a single {@link AutoCloseable} is added twice, it will be
     * {@link AutoCloseable#close() closed} twice.</p>
     *
     * <p>Ignores null values.</p>
     *
     * @param terminables the closables to bind
     * @return this (for chaining)
     */
    default CompositeTerminable withAll(Terminable... terminables) {
        for (Terminable terminable : terminables) {
            if (terminable == null) {
                continue;
            }
            bind(terminable);
        }
        return this;
    }

    /**
     * Binds all given {@link AutoCloseable} with this composite closable.
     *
     * <p>Note that implementations do not keep track of duplicate contained
     * terminables. If a single {@link AutoCloseable} is added twice, it will be
     * {@link AutoCloseable#close() closed} twice.</p>
     *
     * <p>Ignores null values.</p>
     *
     * @param terminables the closables to bind
     * @return this (for chaining)
     */
    default CompositeTerminable withAll(Iterable<? extends Terminable> terminables) {
        for (Terminable terminable : terminables) {
            if (terminable == null) {
                continue;
            }
            bind(terminable);
        }
        return this;
    }

    @NotNull
    @Override
    default <T extends Terminable> T bind(@NotNull T terminable) {
        with(terminable);
        return terminable;
    }

    /**
     * Removes instances which have already been terminated.
     */
    void cleanup();

}
