package io.fairyproject.event.impl;

import io.fairyproject.event.Cancellable;

public class TestEvent implements Cancellable {

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
