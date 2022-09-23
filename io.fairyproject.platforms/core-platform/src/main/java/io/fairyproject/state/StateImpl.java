package io.fairyproject.state;

import io.fairyproject.state.trigger.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class StateImpl<S, T> implements State<S, T> {

    @NotNull
    private final StateMachine<S, T> machine;
    private final List<StateHandler<S, T>> handlers;
    private final Map<Trigger<T>, TriggerTask<S, T>> triggers;
    private Consumer<Trigger<T>> unhandledTrigger;

    public StateImpl(@NotNull StateMachine<S, T> machine) {
        this.machine = machine;
        this.handlers = new ArrayList<>();
        this.triggers = new ConcurrentHashMap<>();
        this.unhandledTrigger = t -> {};
    }

    @Override
    public @NotNull StateMachine<S, T> machine() {
        return this.machine;
    }

    @Override
    public @NotNull State<S, T> handler(@NotNull StateHandler<S, T> handler) {
        this.handlers.add(handler);
        return this;
    }

    @Override
    public @NotNull List<StateHandler<S, T>> handlers() {
        return this.handlers;
    }

    @Override
    public @NotNull State<S, T> when(@NotNull Trigger<T> t, @NotNull Consumer<TriggerTask<S, T>> config) {
        TriggerTask<S, T> triggerTask = this.triggers.computeIfAbsent(t, i -> new TriggerTaskImpl<>(t, this));
        config.accept(triggerTask);

        return this;
    }

    @Override
    public void unhandled(@NotNull Consumer<Trigger<T>> consumer) {
        this.unhandledTrigger = consumer;
    }

    @Override
    public void fire(@NotNull Trigger<T> t) {
        TriggerTask<S, T> triggerTask = this.triggers.get(t);
        if (triggerTask != null) {
            triggerTask.fire();
            return;
        }

        this.unhandledTrigger.accept(t);
    }
}
