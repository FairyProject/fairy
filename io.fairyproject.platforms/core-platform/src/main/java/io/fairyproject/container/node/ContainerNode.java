package io.fairyproject.container.node;

import io.fairyproject.container.controller.node.NodeController;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ContainerNode extends Terminable, TerminableConsumer {

    static ContainerNode create(String name) {
        return new ContainerNodeImpl(name);
    }

    @NotNull String name();

    @Contract("_ -> this")
    @NotNull ContainerNode addObj(@NotNull ContainerObj obj);

    @Nullable ContainerObj getObj(@NotNull Class<?> objectType);

    @Contract("_ -> this")
    @NotNull ContainerNode addChild(@NotNull ContainerNode node);

    @Contract("_ -> this")
    @NotNull ContainerNode removeChild(@NotNull ContainerNode node);

    @NotNull ContainerNode addController(@NotNull NodeController controller);

    @NotNull ContainerNode removeController(@NotNull NodeController controller);

    @NotNull List<NodeController> controllers();

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
