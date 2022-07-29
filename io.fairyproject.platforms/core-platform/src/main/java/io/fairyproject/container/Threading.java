package io.fairyproject.container;

import io.fairyproject.util.AsyncUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;

/**
 * The ThreadingMode for container object initialization
 *
 * @since 0.5.2b1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Threading {

    Mode value();

    enum Mode {
        /**
         * Sync will force this container main threaded and no parallel execution allowed on initialize state
         */
        SYNC {
            @Override
            public CompletableFuture<?> execute(Runnable runnable) {
                runnable.run();
                return AsyncUtils.empty();
            }
        },
        /**
         * Async will let the container async threaded and there could have multiple other container initializing in parallel on initialize state
         */
        ASYNC {
            @Override
            public CompletableFuture<?> execute(Runnable runnable) {
                return CompletableFuture.runAsync(runnable);
            }
        };

        public CompletableFuture<?> execute(Runnable runnable) {
            throw new UnsupportedOperationException();
        }
    }

}
