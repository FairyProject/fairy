package io.fairyproject.container.node;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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

}
