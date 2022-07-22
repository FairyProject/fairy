package io.fairyproject.container.object.lifecycle;

/**
 * The Programmatic Approach of annotation processor
 * You can listen to current container object's life cycle by overwriting the methods in this interface
 */
public interface ILifeCycle {

    default void onPreInit() {
    }

    default void onPostInit() {
    }

    default void onPreDestroy() {
    }

    default void onPostDestroy() {
    }

}
