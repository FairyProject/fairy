package io.fairyproject.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class CompletableFutureUtils {

    public <T> CompletableFuture<T> failureOf(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);

        return future;
    }

}
