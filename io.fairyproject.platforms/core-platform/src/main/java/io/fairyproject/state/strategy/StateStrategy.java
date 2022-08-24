package io.fairyproject.state.strategy;

import io.fairyproject.state.State;
import org.jetbrains.annotations.NotNull;

/**
 * State strategy is a class to provide functional programming style to add multiple strategies into one state
 * To implement a strategy it is suggested to extend {@link StateStrategyBase}
 */
public interface StateStrategy {

    void onStart();

    void onUpdate();

    void onSuspend();

    void onEnded();

    void onPause();

    void onUnpause();

    @NotNull State parent();

    void setParent(@NotNull State state);

    int priority();

    enum Type {
        BEFORE, AFTER
    }

}
