/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.state;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface TransitionBuilder {

    /**
     * Handle transition for a specific signal
     *
     * @param signal the signal
     * @return the transition config builder
     */
    @Contract("_ -> this")
    @NotNull TransitionHandle when(@NotNull Signal signal);

    /**
     * Handle transition for a specific signal
     *
     * @param signal the signal
     * @param handle the handle
     * @return this
     */
    @NotNull TransitionBuilder when(@NotNull Signal signal, @NotNull Consumer<TransitionHandle> handle);

    /**
     * Handle transition for a specific signal
     *
     * @param signal the signal
     * @param handle the handle
     * @return this
     */
    @NotNull TransitionBuilder when(@NotNull Signal signal, @NotNull TransitionHandle handle);

    /**
     * Run the runnable when the signal is received
     *
     * @param signal the signal
     * @param runnable the runnable
     * @return this
     */
    @Contract("_, _ -> this")
    @NotNull TransitionBuilder when(@NotNull Signal signal, @NotNull Runnable runnable);

    interface TransitionHandle {

        @Contract("_ -> this")
        @NotNull TransitionHandle to(@NotNull State state);

        @Contract("_ -> this")
        @NotNull TransitionHandle to(@NotNull String state);

        @Contract("_ -> this")
        @NotNull TransitionHandle run(@NotNull Runnable runnable);

    }

}
