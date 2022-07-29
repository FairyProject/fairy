package io.fairyproject.event;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.Service;

@Service
public class GlobalEventNode extends EventNodeImpl<Event> {

    @Autowired
    private static GlobalEventNode INSTANCE;
    public static EventNode<Event> get() {
        return INSTANCE;
    }

    public GlobalEventNode() {
        super("global", EventFilter.ALL, null);
    }
}
