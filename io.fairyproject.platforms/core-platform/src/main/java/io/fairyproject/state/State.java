package io.fairyproject.state;

import io.fairyproject.state.trigger.Trigger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * The state system from fairy framework
 *
 * @param <S> the state type
 * @param <T> the trigger type
 */
public interface State<S, T> {

    /**
     * The state machine of the state
     *
     * @return the state machine
     */
    @NotNull StateMachine<S, T> machine();

    /**
     * Add a new handler to the state
     *
     * @param handler the handler
     * @return this
     */
    @Contract("_ -> this")
    @NotNull State<S, T> handler(@NotNull StateHandler<S, T> handler);

    /**
     * The handlers of the state
     *
     * @return the handlers
     */
    @NotNull List<StateHandler<S, T>> handlers();

    /**
     * Handle the start of the state
     *
     * @param runnable the runnable
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default State<S, T> handleStart(@NotNull Runnable runnable) {
        return handler(StateHandler.<S, T>builder()
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
    @NotNull default State<S, T> handleTick(@NotNull Runnable runnable) {
        return handler(StateHandler.<S, T>builder()
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
    @NotNull default State<S, T> handleStop(@NotNull Runnable runnable) {
        return handler(StateHandler.<S, T>builder()
                .onStop(runnable)
                .build());
    }

    /**
     * Handle the start of the state
     * @param consumer the consumer
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default State<S, T> handleStart(@NotNull Consumer<Trigger<T>> consumer) {
        return handler(StateHandler.<S, T>builder()
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
    @NotNull default State<S, T> handleStop(@NotNull Consumer<Trigger<T>> consumer) {
        return handler(StateHandler.<S, T>builder()
                .onStop(consumer)
                .build());
    }

    /**
     * Handle a trigger of the state
     *
     * @param t the trigger
     * @param config configuration of the trigger
     * @return this
     */
    @Contract("_, _ -> this")
    @NotNull default State<S, T> when(@NotNull T t, @NotNull Consumer<TriggerTask<S, T>> config) {
        return this.when(Trigger.of(t), config);
    }

    /**
     * Handle a trigger of the state
     *
     * @param trigger the trigger
     * @param config configuration of the trigger
     * @return this
     */
    @Contract("_, _ -> this")
    @NotNull State<S, T> when(@NotNull Trigger<T> trigger, @NotNull Consumer<TriggerTask<S, T>> config);

    /**
     * Listen to unhandled triggers
     *
     * @param consumer the consumer
     */
    void unhandled(@NotNull Consumer<Trigger<T>> consumer);

    /**
     * Fire a trigger
     *
     * @param trigger the trigger
     */
    void fire(@NotNull Trigger<T> trigger);

}
