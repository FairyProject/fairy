package io.fairyproject.state;

import io.fairyproject.state.impl.TimeoutStateHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface StateHandler {

    static Builder builder() {
        return new Builder();
    }

    static StateHandler timeout(Duration duration) {
        return new TimeoutStateHandler(duration);
    }

    /**
     * On start of the state
     *
     * @param stateMachine the state machine
     * @param state        the state
     * @param signal       the signal
     */
    default void onStart(@NotNull StateMachine stateMachine, @NotNull State state, @Nullable Signal signal) {
        // to be overridden
    }

    /**
     * On tick of the state
     *
     * @param stateMachine the state machine
     * @param state        the state
     */
    default void onTick(@NotNull StateMachine stateMachine, @NotNull State state) {
        // to be overridden
    }

    /**
     * On end of the state
     *
     * @param stateMachine the state machine
     * @param state        the state
     * @param signal       the signal
     */
    default void onStop(@NotNull StateMachine stateMachine, @NotNull State state, @Nullable Signal signal) {
        // to be overridden
    }

    class Builder {
        private BiConsumer<State, Signal> onStart;
        private Consumer<State> onTick;
        private BiConsumer<State, Signal> onStop;

        @Contract("_ -> this")
        public Builder onStart(@NotNull BiConsumer<State, Signal> onStart) {
            this.onStart = onStart;
            return this;
        }

        @Contract("_ -> this")
        public Builder onStop(@NotNull BiConsumer<State, Signal> onStop) {
            this.onStop = onStop;
            return this;
        }

        @Contract("_ -> this")
        public Builder onStart(@NotNull Consumer<Signal> onStart) {
            return onStart((s, t) -> onStart.accept(t));
        }

        @Contract("_ -> this")
        public Builder onTick(@NotNull Consumer<State> onTick) {
            this.onTick = onTick;
            return this;
        }

        @Contract("_ -> this")
        public Builder onStop(@NotNull Consumer<Signal> onStop) {
            return onStop((s, t) -> onStop.accept(t));
        }

        @Contract("_ -> this")
        public Builder onStart(@NotNull Runnable onStart) {
            return onStart((s, t) -> onStart.run());
        }

        @Contract("_ -> this")
        public Builder onTick(@NotNull Runnable onTick) {
            return onTick(s -> onTick.run());
        }

        @Contract("_ -> this")
        public Builder onStop(@NotNull Runnable onStop) {
            return onStop((s, t) -> onStop.run());
        }

        public StateHandler build() {
            return new StateHandler() {
                @Override
                public void onStart(@NotNull StateMachine stateMachine, @NotNull State state, @Nullable Signal trigger) {
                    if (onStart != null) {
                        onStart.accept(state, trigger);
                    }
                }

                @Override
                public void onTick(@NotNull StateMachine stateMachine, @NotNull State state) {
                    if (onTick != null) {
                        onTick.accept(state);
                    }
                }

                @Override
                public void onStop(@NotNull StateMachine stateMachine, @NotNull State state, @Nullable Signal trigger) {
                    if (onStop != null) {
                        onStop.accept(state, trigger);
                    }
                }
            };
        }
    }
}
