package io.fairyproject.container.scope;

public enum NodeScope {
    /**
     * The node is global and shared between all nodes.
     */
    GLOBAL,
    /**
     * The node is isolated and not shared between any nodes.
     */
    ISOLATED
}
