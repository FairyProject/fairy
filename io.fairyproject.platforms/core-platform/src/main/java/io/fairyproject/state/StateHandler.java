package io.fairyproject.state;

import io.fairyproject.state.impl.TimeoutStateHandler;
import io.fairyproject.state.trigger.Trigger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface StateHandler<S, T> {

    static <S, T> Builder<S, T> builder() {
        return new Builder<>();
    }

    static <S, T> StateHandler<S, T> timeout(Duration duration) {
        return new TimeoutStateHandler<>(duration);
    }

    /**
     * On start of the state
     *
     * @param stateMachine the state machine
     * @param state the state
     * @param trigger the trigger
     */
    void onStart(@NotNull StateMachine<S, T> stateMachine, @NotNull S state, @Nullable Trigger<T> trigger);

    /**
     * On tick of the state
     *
     * @param stateMachine the state machine
     * @param state the state
     */
    void onTick(@NotNull StateMachine<S, T> stateMachine, @NotNull S state);

    /**
     * On end of the state
     *
     * @param stateMachine the state machine
     * @param state the state
     * @param trigger the trigger
     */
    void onStop(@NotNull StateMachine<S, T> stateMachine, @NotNull S state, @Nullable Trigger<T> trigger);

    class Builder<S, T> {
        private BiConsumer<S, Trigger<T>> onStart;
        private Consumer<S> onTick;
        private BiConsumer<S, Trigger<T>> onStop;

        @Contract("_ -> this")
        public Builder<S, T> onStart(@NotNull BiConsumer<S, Trigger<T>> onStart) {
            this.onStart = onStart;
            return this;
        }

        @Contract("_ -> this")
        public Builder<S, T> onStop(@NotNull BiConsumer<S, Trigger<T>> onStop) {
            this.onStop = onStop;
            return this;
        }

        @Contract("_ -> this")
        public Builder<S, T> onStart(@NotNull Consumer<Trigger<T>> onStart) {
            return onStart((s, t) -> onStart.accept(t));
        }

        @Contract("_ -> this")
        public Builder<S, T> onTick(@NotNull Consumer<S> onTick) {
            this.onTick = onTick;
            return this;
        }

        @Contract("_ -> this")
        public Builder<S, T> onStop(@NotNull Consumer<Trigger<T>> onStop) {
            return onStop((s, t) -> onStop.accept(t));
        }

        @Contract("_ -> this")
        public Builder<S, T> onStart(@NotNull Runnable onStart) {
            return onStart((s, t) -> onStart.run());
        }

        @Contract("_ -> this")
        public Builder<S, T> onTick(@NotNull Runnable onTick) {
            return onTick(s -> onTick.run());
        }

        @Contract("_ -> this")
        public Builder<S, T> onStop(@NotNull Runnable onStop) {
            return onStop((s, t) -> onStop.run());
        }

        public StateHandler<S, T> build() {
            return new StateHandler<S, T>() {
                @Override
                public void onStart(@NotNull StateMachine<S, T> stateMachine, @NotNull S state, @Nullable Trigger<T> trigger) {
                    if (onStart != null) {
                        onStart.accept(state, trigger);
                    }
                }

                @Override
                public void onTick(@NotNull StateMachine<S, T> stateMachine, @NotNull S state) {
                    if (onTick != null) {
                        onTick.accept(state);
                    }
                }

                @Override
                public void onStop(@NotNull StateMachine<S, T> stateMachine, @NotNull S state, @Nullable Trigger<T> trigger) {
                    if (onStop != null) {
                        onStop.accept(state, trigger);
                    }
                }
            };
        }
    }
}
