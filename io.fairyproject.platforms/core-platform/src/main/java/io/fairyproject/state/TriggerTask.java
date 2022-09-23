package io.fairyproject.state;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TriggerTask<S, T> {

    /**
     * The state machine of the trigger task
     *
     * @return the state machine
     */
    @NotNull default StateMachine<S, T> machine() {
        return this.state().machine();
    }

    /**
     * The state of the trigger task
     *
     * @return the state
     */
    @NotNull State<S, T> state();

    /**
     * Run a task when trigger is fired
     *
     * @param runnable the runnable
     * @return this
     */
    @Contract("_ -> this")
    TriggerTask<S, T> run(Runnable runnable);

    /**
     * Transist to a new state when trigger is fired
     *
     * @param state the state
     * @return this
     */
    @Contract("_ -> this")
    TriggerTask<S, T> to(S state);

    /**
     * End the state machine when trigger is fired
     *
     * @return this
     */
    @Contract("-> this")
    TriggerTask<S, T> end();

    /**
     * Fire the trigger
     */
    @ApiStatus.Internal
    void fire();

}
