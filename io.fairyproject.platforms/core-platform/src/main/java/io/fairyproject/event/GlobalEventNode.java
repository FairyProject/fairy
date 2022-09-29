package io.fairyproject.event;

import io.fairyproject.Fairy;
import io.fairyproject.internal.InternalProcess;

public class GlobalEventNode extends EventNodeImpl<Event> implements InternalProcess {

    public static EventNode<Event> get() {
        return Fairy.getGlobalEventNode();
    }

    public GlobalEventNode() {
        super("global", EventFilter.ALL, null);
    }
}
