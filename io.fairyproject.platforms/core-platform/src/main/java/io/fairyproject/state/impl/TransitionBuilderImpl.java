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

package io.fairyproject.state.impl;

import io.fairyproject.state.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TransitionBuilderImpl implements TransitionBuilder {

    private final Map<Signal, TransitionHandle> transitions = new HashMap<>();

    @Override
    public @NotNull TransitionHandle when(@NotNull Signal signal) {
        return this.transitions.computeIfAbsent(signal, TransitionHandleImpl::new);
    }

    @Override
    public @NotNull TransitionBuilder when(@NotNull Signal signal, @NotNull Consumer<TransitionHandle> handle) {
        handle.accept(this.when(signal));
        return this;
    }

    @Override
    public @NotNull TransitionBuilder when(@NotNull Signal signal, @NotNull TransitionHandle handle) {
        this.transitions.put(signal, handle);
        return this;
    }

    @Override
    public @NotNull TransitionBuilder when(@NotNull Signal signal, @NotNull Runnable runnable) {
        this.when(signal).run(runnable);
        return this;
    }

    public Transition build(StateMachine stateMachine) {
        return signal -> {
            TransitionHandleImpl handle = (TransitionHandleImpl) this.transitions.getOrDefault(signal, null);
            if (handle != null) {
                handle.function.accept(stateMachine);
            }
        };
    }

    public static class TransitionHandleImpl implements TransitionHandle {

        private final Signal signal;
        private Consumer<StateMachine> function;

        public TransitionHandleImpl(Signal signal) {
            this.signal = signal;
        }

        @Override
        public @NotNull TransitionHandle to(@NotNull State state) {
            this.function = stateMachine -> stateMachine.transform(state, signal);
            return this;
        }

        @Override
        public @NotNull TransitionHandle to(@NotNull String state) {
            return this.to(State.of(state));
        }

        @Override
        public @NotNull TransitionHandle run(@NotNull Runnable runnable) {
            this.function = stateMachine -> runnable.run();
            return this;
        }
    }
}
