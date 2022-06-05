package io.fairyproject.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class CompletableFutureUtils {

    public <T> CompletableFuture<T> failureOf(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);

        return future;
    }

    public CompletableFuture<?> allOf(Collection<CompletableFuture<?>> futures) {
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
