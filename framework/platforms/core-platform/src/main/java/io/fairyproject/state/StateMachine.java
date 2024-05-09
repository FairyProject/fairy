package io.fairyproject.state;

import io.fairyproject.event.EventNode;
import io.fairyproject.state.event.StateMachineEvent;
import io.fairyproject.state.impl.StateMachineBuilderImpl;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The state machine system from fairy framework
 */
public interface StateMachine extends Terminable, TerminableConsumer {

    /**
     * Create a new state machine builder
     *
     * @return the state machine builder
     */
    static StateMachineBuilder builder() {
        return new StateMachineBuilderImpl();
    }

    /**
     * The current state of the state machine
     *
     * @return the current state
     */
    @Nullable State getCurrentState();

    /**
     * Transition to a new state
     *
     * @param state the state
     * @return the previous state, null if there was no previous state
     */
    @Nullable default State transform(@NotNull State state) {
        return transform(state, Signal.UNDEFINED);
    }

    /**
     * Transition to a new state
     *
     * @param state the state to transform
     * @return the previous state, null if there was no previous state
     */
    @Nullable State transform(@NotNull State state, @Nullable Signal signal);

    /**
     * Tick the state machine
     */
    void tick();

    /**
     * Stop the state machine
     *
     * @param signal the signal
     */
    void stop(@NotNull Signal signal);

    /**
     * Fire a signal to the state machine
     *
     * @param signal the signal
     */
    void signal(@NotNull Signal signal);

    /**
     * The event node of the state machine
     *
     * @return the event node
     */
    EventNode<StateMachineEvent> getEventNode();

}
