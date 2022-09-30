package io.fairyproject.event;

import io.fairyproject.Fairy;
import io.fairyproject.internal.Process;

public class GlobalEventNode extends EventNodeImpl<Event> implements Process {

    public static EventNode<Event> get() {
        return Fairy.getGlobalEventNode();
    }

    public GlobalEventNode() {
        super("global", EventFilter.ALL, null);
    }
}
