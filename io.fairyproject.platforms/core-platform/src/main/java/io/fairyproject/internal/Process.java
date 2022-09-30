package io.fairyproject.internal;

/**
 * A internal process that is used to manage internal instances.
 */
public interface Process {

    default void preload() {
    }

    default void load() {
    }

    default void enable() {
    }

    default void destroy() {
    }

}
