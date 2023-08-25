package io.fairyproject.container.node;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ContainerLogger;
import io.fairyproject.container.ContainerReference;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.controller.node.NodeController;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ContainerNodeImpl implements ContainerNode {
    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();
    private final Map<Class<?>, ContainerObj> objects = new ConcurrentHashMap<>(16);
    private final Set<ContainerNode> childNodes = ConcurrentHashMap.newKeySet();
    private final List<NodeController> controllers = new ArrayList<>();
    private final Graph<ContainerObj> graph = new GraphImpl();
    private final String name;

    public ContainerNodeImpl(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }


    @Override
    public @NotNull ContainerNode addObj(@NotNull ContainerObj obj) {
        ConditionUtils.is(!this.isResolved(), "The ContainerNode has already been resolved.");
        this.objects.put(obj.type(), obj);
        ContainerReference.setObj(obj.type(), obj);
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
    public @NotNull ContainerNode addController(@NotNull NodeController controller) {
        this.controllers.add(controller);
        return this;
    }

    @Override
    public @NotNull ContainerNode removeController(@NotNull NodeController controller) {
        this.controllers.remove(controller);
        return this;
    }

    @Override
    public @NotNull List<NodeController> controllers() {
        return Collections.unmodifiableList(this.controllers);
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

        boolean success = true;
        for (ContainerObj obj : this.objects.values()) {
            if (!success)
                break;

            for (ContainerObj.DependEntry entry : obj.dependEntries()) {
                final Class<?> dependClass = entry.getDependClass();

                if (!ContainerReference.hasObj(dependClass)) {
                    ContainerLogger.report(this, obj, null,
                            "Unknown dependency: " + dependClass.getName(),
                            " ",
                            "Maybe you forgot to register it? Make sure the dependency is marked as @InjectableComponent",
                            "and you have the class in the classpath.");
                    success = false;
                    break;
                }
            }
            this.graph.add(obj);
        }

        if (!success)
            return this;

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
    public void close() throws Exception {
        for (ContainerNode node : this.childNodes) {
            node.closeAndReportException();
        }
        this.graph.forEachCounterClockwise(this::handleCloseObj);
        this.compositeTerminable.close();
    }

    private void handleCloseObj(ContainerObj obj) {
        obj.setLifeCycle(LifeCycle.PRE_DESTROY);
        try {
            obj.close();
        } catch (Throwable t) {
            SneakyThrowUtil.sneakyThrow(t);
        } finally {
            obj.setLifeCycle(LifeCycle.POST_DESTROY);
            for (ContainerController controller : ContainerContext.get().controllers()) {
                ThrowingRunnable.sneaky(() -> controller.removeContainerObject(obj)).run();
            }
            ContainerReference.setObj(obj.type(), null);
        }
    }

    @NotNull
    @Override
    public <T extends Terminable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
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
            for (ContainerObj.DependEntry entry : parent.dependEntries()) {
                final Class<?> type = entry.getDependClass();
                final ContainerObj obj = getObj(type);

                // it's not in current node but has container object
                // assuming it's registered by other node.
                if (obj == null && ContainerReference.hasObj(type)) {
                    continue;
                }
                retVal.add(obj);
            }

            return retVal.toArray(new ContainerObj[0]);
        }
    }
}
