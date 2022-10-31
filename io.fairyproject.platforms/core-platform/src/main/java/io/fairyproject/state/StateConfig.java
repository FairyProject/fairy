package io.fairyproject.state;

import io.fairyproject.event.EventNode;
import io.fairyproject.state.event.StateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The state system from fairy framework
 */
public interface StateConfig {

    /**
     * Get the state of the config
     *
     * @return the state
     */
    @NotNull State getState();

    /**
     * The state machine of the state
     *
     * @return the state machine
     */
    @NotNull StateMachine getStateMachine();

    /**
     * The handlers of the state
     *
     * @return the handlers
     */
    @NotNull StateHandler[] getHandlers();

    /**
     * The event node of the state
     *
     * @return the event node
     */
    @NotNull EventNode<StateEvent> getEventNode();

    /**
     * Get the state handler by the class
     *
     * @param handlerClass the handler class
     * @return the state handler, null if not found
     */
    @Nullable StateHandler getHandler(@NotNull Class<? extends StateHandler> handlerClass);

}
