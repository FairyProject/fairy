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

package io.fairyproject.util.terminable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An extension of {@link AutoCloseable}.
 *
 * @author lucko
 */
@FunctionalInterface
public interface Terminable {
    Terminable EMPTY = () -> {};

    /**
     * Closes this resource.
     */
    void close() throws Exception;

    /**
     * Gets if the object represented by this instance is already permanently closed.
     *
     * @return true if this terminable is closed permanently
     */
    default boolean isClosed() {
        return false;
    }

    /**
     * Silently closes this resource, and returns the exception if one is thrown.
     *
     * @return the exception is one is thrown
     */
    @Nullable
    default Exception closeSilently() {
        try {
            close();
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    /**
     * Closes this resource, and prints the exception if one is thrown.
     */
    default void closeAndReportException() {
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Binds this terminable with a terminable consumer
     *
     * @param consumer the terminable consumer
     */
    default void bindWith(@NotNull TerminableConsumer consumer) {
        consumer.bind(this);
    }

}
