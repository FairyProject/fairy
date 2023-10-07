package io.fairyproject.container.node;

import io.fairyproject.container.binder.ContainerObjectBinder;
import io.fairyproject.container.object.ContainerObj;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ContainerNode {

    static ContainerNode create(String name, ContainerObjectBinder binder) {
        return new ContainerNodeImpl(name, binder);
    }

    @NotNull String name();

    @Contract("_ -> this")
    @NotNull ContainerNode addObj(@NotNull ContainerObj obj);

    @Nullable ContainerObj getObj(@NotNull Class<?> objectType);

    @Contract("_ -> this")
    @NotNull ContainerNode addChild(@NotNull ContainerNode node);

    @Contract("_ -> this")
    @NotNull ContainerNode removeChild(@NotNull ContainerNode node);

    @NotNull Set<ContainerNode> childs();

    @NotNull Set<ContainerObj> all();

    @NotNull Graph<ContainerObj> graph();

    /**
     * Resolve the node.
     * After the node being resolved, the node will no longer be mutable,
     * Which can be checked through {@link ContainerNode#isResolved()}.
     *
     * @return this
     */
    @Contract("-> this")
    @NotNull ContainerNode resolve();

    boolean isResolved();


    CompletableFuture<?> forEachClockwiseAwait(Function<ContainerObj, CompletableFuture<?>> function);


    CompletableFuture<?> forEachCounterClockwiseAwait(Function<ContainerObj, CompletableFuture<?>> function);

}
