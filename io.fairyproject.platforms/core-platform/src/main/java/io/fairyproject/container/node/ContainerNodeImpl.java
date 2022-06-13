package io.fairyproject.container.node;

import com.sun.corba.se.impl.orbutil.graph.GraphImpl;
import io.fairyproject.container.ContainerRef;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerNodeImpl implements ContainerNode {
    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();
    private final Map<Class<?>, ContainerObj> objects = new ConcurrentHashMap<>(16);
    private final Set<ContainerNode> childNodes = ConcurrentHashMap.newKeySet();
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
        ConditionUtils.check(!this.isResolved(), "The ContainerNode has already been resolved.");
        this.objects.put(obj.type(), obj);
        ContainerRef.setObj(obj.type(), obj);
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
        for (ContainerObj obj : this.objects.values()) {
            for (ContainerObj.DependEntry entry : obj.dependEntries()) {
                final Class<?> dependClass = entry.getDependClass();
                final ServiceDependencyType type = entry.getDependType();

                if (ContainerRef.hasObj(dependClass))
                    continue;
                if (type == ServiceDependencyType.FORCE) {
                    throw new IllegalArgumentException("Cannot find container obj: " + dependClass);
                } else {
                    // TODO - sub disable
                    continue;
                }
            }
            this.graph.add(obj);
        }
        this.graph.resolve();
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
            ContainerRef.setObj(obj.type(), null);
        }
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
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
                if (obj == null && ContainerRef.hasObj(type)) {
                    continue;
                }
                retVal.add(obj);
            }

            return retVal.toArray(new ContainerObj[0]);
        }
    }
}
