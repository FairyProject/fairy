package io.fairyproject.container.node;

import io.fairyproject.container.ContainerLogger;
import io.fairyproject.container.binder.ContainerObjectBinder;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.ConditionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ContainerNodeImpl implements ContainerNode {

    private final String name;
    private final ContainerObjectBinder binder;
    private final Map<Class<?>, ContainerObj> objects;
    private final Set<ContainerNode> childNodes;
    private final Graph<ContainerObj> graph;

    public ContainerNodeImpl(String name, ContainerObjectBinder binder) {
        this.name = name;
        this.binder = binder;
        this.objects = new ConcurrentHashMap<>(16);
        this.childNodes = ConcurrentHashMap.newKeySet();
        this.graph = new GraphImpl();
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }


    @Override
    public @NotNull ContainerNode addObj(@NotNull ContainerObj obj) {
        ConditionUtils.is(!this.isResolved(), "The ContainerNode has already been resolved.");
        this.objects.put(obj.getType(), obj);
        return this;
    }

    @Override
    public @Nullable ContainerObj getObj(@NotNull Class<?> objectType) {
        final ContainerObj obj = this.objects.get(objectType);
        if (obj != null)
            return obj;

        for (ContainerNode node : childNodes) {
            final ContainerObj nodeObj = node.getObj(objectType);
            if (nodeObj != null) {
                return nodeObj;
            }
        }

        return null;
    }

    @Override
    public @NotNull ContainerNode addChild(@NotNull ContainerNode node) {
        this.childNodes.add(node);
        return this;
    }

    @Override
    public @NotNull ContainerNode removeChild(@NotNull ContainerNode node) {
        this.childNodes.remove(node);
        return this;
    }

    @Override
    public @NotNull Set<ContainerNode> childs() {
        return Collections.unmodifiableSet(this.childNodes);
    }

    @Override
    public @NotNull Set<ContainerObj> all() {
        Set<ContainerObj> retVal = new HashSet<>(this.objects.values());
        for (ContainerNode childNode : this.childNodes) {
            retVal.addAll(childNode.all());
        }

        return retVal;
    }

    @Override
    public @NotNull Graph<ContainerObj> graph() {
        return this.graph;
    }

    @Override
    public @NotNull ContainerNode resolve() {
        if (this.isResolved())
            return this;

        for (ContainerObj obj : this.objects.values()) {
            for (Class<?> dependClass : obj.getDependencies()) {
                if (!this.binder.isBound(dependClass)) {
                    ContainerLogger.report(this, obj, null,
                            "Unknown dependency: " + dependClass.getName(),
                            " ",
                            "Maybe you forgot to register it? Make sure the dependency is marked as @InjectableComponent",
                            "and you have the class in the classpath.");
                    return this;
                }
            }
            this.graph.add(obj);
        }

        this.graph.setAutoAdd(true);
        this.graph.resolve();

        for (ContainerNode childNode : this.childNodes) {
            childNode.resolve();
        }
        return this;
    }

    @Override
    public boolean isResolved() {
        return this.graph.isResolved();
    }

    @Override
    public CompletableFuture<?> forEachClockwiseAwait(Function<ContainerObj, CompletableFuture<?>> function) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(this.graph.forEachClockwiseAwait(function));
        for (ContainerNode node : this.childNodes) {
            futures.add(node.forEachClockwiseAwait(function));
        }

        return AsyncUtils.allOf(futures);
    }

    @Override
    public CompletableFuture<?> forEachCounterClockwiseAwait(Function<ContainerObj, CompletableFuture<?>> function) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (ContainerNode node : this.childNodes) {
            futures.add(node.forEachCounterClockwiseAwait(function));
        }
        futures.add(this.graph.forEachCounterClockwiseAwait(function));

        return AsyncUtils.allOf(futures);
    }

    private class GraphImpl extends Graph<ContainerObj> {

        @Override
        public ContainerObj[] depends(ContainerObj parent) {
            final List<ContainerObj> retVal = new ArrayList<>();
            for (Class<?> type : parent.getDependencies()) {
                final ContainerObj obj = binder.getBinding(type);

                if (obj == null)
                    throw new IllegalStateException("Unknown dependency: " + type.getName());
                retVal.add(obj);
            }

            return retVal.toArray(new ContainerObj[0]);
        }
    }
}
