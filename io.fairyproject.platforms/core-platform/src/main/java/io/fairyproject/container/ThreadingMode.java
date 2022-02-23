package io.fairyproject.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ThreadingMode for container object initialization
 *
 * @since 0.5.2b1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadingMode {

    Mode value();

    enum Mode {
        /**
         * Sync will force this container main threaded and no parallel execution allowed on initialize state
         */
        SYNC,
        /**
         * Async will let the container async threaded and there could have multiple other container initializing in parallel on initialize state
         */
        ASYNC
    }

}
