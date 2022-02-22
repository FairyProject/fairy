package io.fairyproject.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class CompletableFutureUtils {

    public CompletableFuture<?> failureOf(Throwable throwable) {
        CompletableFuture<?> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);

        return future;
    }

}
