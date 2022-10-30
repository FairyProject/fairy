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

public interface StateConfigBuilder {

    /**
     * Add a new handler to the state
     *
     * @param handler the handler
     * @return this
     */
    @Contract("_ -> this")
    @NotNull StateConfigBuilder handler(@NotNull StateHandler handler);


    /**
     * Handle the start of the state
     *
     * @param runnable the runnable
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default StateConfigBuilder handleStart(@NotNull Runnable runnable) {
        return handler(StateHandler.builder()
                .onStart(runnable)
                .build());
    }

    /**
     * Handle the ticking of the state
     *
     * @param runnable the runnable
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default StateConfigBuilder handleTick(@NotNull Runnable runnable) {
        return handler(StateHandler.builder()
                .onTick(runnable)
                .build());
    }

    /**
     * Handle the end of the state
     *
     * @param runnable the runnable
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default StateConfigBuilder handleStop(@NotNull Runnable runnable) {
        return handler(StateHandler.builder()
                .onStop(runnable)
                .build());
    }

    /**
     * Handle the start of the state
     * @param consumer the consumer
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default StateConfigBuilder handleStart(@NotNull Consumer<Signal> consumer) {
        return handler(StateHandler.builder()
                .onStart(consumer)
                .build());
    }

    /**
     * Handle the end of the state
     *
     * @param consumer the consumer
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default StateConfigBuilder handleStop(@NotNull Consumer<Signal> consumer) {
        return handler(StateHandler.builder()
                .onStop(consumer)
                .build());
    }

}
