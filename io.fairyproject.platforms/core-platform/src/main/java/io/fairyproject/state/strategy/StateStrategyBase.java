package io.fairyproject.state.strategy;

import io.fairyproject.state.State;
import org.jetbrains.annotations.NotNull;

public abstract class StateStrategyBase implements StateStrategy {

    private final int priority;
    private State parent;

    public StateStrategyBase(int priority) {
        this.priority = priority;
    }

    @Override
    public @NotNull State parent() {
        return this.parent;
    }

    public void setParent(@NotNull State state) {
        this.parent = state;
    }

    @Override
    public int priority() {
        return this.priority;
    }
}
