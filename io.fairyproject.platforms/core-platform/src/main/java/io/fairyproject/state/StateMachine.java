package io.fairyproject.state;

import io.fairyproject.state.trigger.Trigger;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * The state machine system from fairy framework
 *
 * @param <S> the state type
 * @param <T> the trigger type
 */
public interface StateMachine<S, T> extends Terminable, TerminableConsumer {

    /**
     * Create a new state machine
     *
     * @return the state machine
     * @param <S> the state type
     * @param <T> the trigger type
     */
    static <S, T> StateMachine<S, T> create() {
        return new StateMachineImpl<>();
    }

    /**
     * Create a new state from the state machine
     *
     * @param state the state entity
     * @return the state implementation
     */
    @NotNull State<S, T> state(@NotNull S state);

    /**
     * The current state of the state machine
     *
     * @return the current state
     */
    @Nullable S current();

    /**
     * Set the interval of the state machine
     *
     * @param interval the interval
     * @return this
     */
    @Contract("_ -> this")
    @NotNull StateMachine<S, T> interval(Duration interval);

    /**
     * Start the state machine
     *
     * @param state the state to start
     * @param trigger the trigger
     * @return this
     */
    @Contract("_, _ -> this")
    @NotNull StateMachine<S, T> start(@NotNull S state, @Nullable Trigger<T> trigger);

    /**
     * Swap the state of the state machine
     *
     * @param state the state to swap
     * @param trigger the trigger
     * @return previous state if have
     */
    @Contract("_, _ -> this")
    @Nullable S swap(S state, Trigger<T> trigger);

    /**
     * Tick the state machine
     *
     * @return this
     */
    @Contract("-> this")
    @NotNull StateMachine<S, T> tick();

    /**
     * Stop the state machine
     *
     * @param trigger the trigger
     * @return this
     */
    @Contract("_ -> this")
    @NotNull StateMachine<S, T> stop(@Nullable Trigger<T> trigger);

    /**
     * The interval of the state machine ticking
     *
     * @return the interval
     */
    @NotNull Duration interval();

    /**
     * Fire a trigger to the state machine
     *
     * @param trigger the trigger
     * @return this
     */
    @Contract("_ -> this")
    @NotNull StateMachine<S, T> fire(@NotNull Trigger<T> trigger);

    /**
     * Fire a trigger to the state machine
     *
     * @param trigger the trigger
     * @return this
     */
    @Contract("_ -> this")
    @NotNull default StateMachine<S, T> fire(@NotNull T trigger) {
        return fire(Trigger.of(trigger));
    }

}
