package io.fairyproject.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class AsyncUtils {

    public final CompletableFuture<?> EMPTY = CompletableFuture.completedFuture(null);

    public <T> CompletableFuture<T> empty() {
        //noinspection unchecked
        return (CompletableFuture<T>) EMPTY;
    }

    public <T> CompletableFuture<T> failureOf(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);

        return future;
    }

    public CompletableFuture<?> allOf(Collection<CompletableFuture<?>> futures) {
        if (futures.isEmpty()) {
            return AsyncUtils.empty();
        }
        if (futures.size() == 1) {
            return futures.iterator().next();
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
