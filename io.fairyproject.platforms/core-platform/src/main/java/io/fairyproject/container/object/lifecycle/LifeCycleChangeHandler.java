package io.fairyproject.container.object.lifecycle;

import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.AsyncUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface LifeCycleChangeHandler extends Function<LifeCycle, CompletableFuture<?>> {

    @Override
    default CompletableFuture<?> apply(LifeCycle lifeCycle) {
        CompletableFuture<?> retVal;

        switch (lifeCycle) {
            case CONSTRUCT:
                retVal = this.onConstruct();
                break;
            case PRE_INIT:
                retVal = this.onPreInit();
                break;
            case POST_INIT:
                retVal = this.onPostInit();
                break;
            case PRE_DESTROY:
                retVal = this.onPreDestroy();
                break;
            case POST_DESTROY:
                retVal = this.onPostDestroy();
                break;
            default:
                throw new IllegalArgumentException("Unsupported Life Cycle change: " + lifeCycle);
        }

        return retVal;
    }

    default void init() {

    }

    default CompletableFuture<?> onConstruct() {
        return AsyncUtils.empty();
    }

    default CompletableFuture<?> onPreInit() {
        return AsyncUtils.empty();
    }

    default CompletableFuture<?> onPostInit() {
        return AsyncUtils.empty();
    }

    default CompletableFuture<?> onPreDestroy() {
        return AsyncUtils.empty();
    }

    default CompletableFuture<?> onPostDestroy() {
        return AsyncUtils.empty();
    }
}
