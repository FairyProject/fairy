package io.fairyproject.container;

import io.fairyproject.util.AsyncUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

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

            @Override
            public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
                return CompletableFuture.completedFuture(supplier.get());
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

            @Override
            public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
                return CompletableFuture.supplyAsync(supplier);
            }

            @Override
            public Executor getExecutor() {
                return CompletableFuture::runAsync;
            }
        };

        public Executor getExecutor() {
            return this::execute;
        }

        public CompletableFuture<?> execute(Runnable runnable) {
            throw new UnsupportedOperationException();
        }

        public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
            throw new UnsupportedOperationException();
        }
    }

}
