package io.fairyproject.container.node;

import io.fairyproject.util.AsyncUtils;
import io.fairyproject.util.ConditionUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An order collection util that can add objects in
 * And lookup them by the order which presents by {@link Graph#depends(Object)} with Topological Sorting
 * If an object is not referenced as a children, it will be head node
 * And the order will be furthered not prioritized if it's referenced more later in the children
 *
 * @param <T> The type of the object that this dependency tree tries to resolve
 */
public abstract class Graph<T> {


    protected final Set<T> objects = new HashSet<>();
    protected final Map<T, List<T>> edges = new HashMap<>();
    private final AtomicBoolean resolved = new AtomicBoolean(false);
    @Getter
    private List<T> nodes;

    public abstract T[] depends(T parent);

    public void handleCycle(T current, List<T> diagram) {
        throw new IllegalArgumentException("Diagram shows this graph contains cycle: " + diagram.stream()
                .map(t -> {
                    if (t == current) {
                        return "*" + t.toString() + "*";
                    }
                    return t.toString();
                })
                .collect(Collectors.joining(","))
        );
    }

    public void add(@NotNull T obj) {
        ConditionUtils.is(!this.resolved.get(), "The DependencyTree was resolved.");
        this.objects.add(obj);
    }

    public Graph<T> resolve() {
        ConditionUtils.is(this.resolved.compareAndSet(false, true), "The DependencyTree was resolved.");
        // Resolve depends to edges
        for (T object : objects) {
            final T[] depends = this.depends(object);
            for (T depend : depends) {
                ConditionUtils.is(
                        this.objects.contains(depend),
                        String.format("%s depend by %s", depend, object)
                );
                this.edges.computeIfAbsent(depend, k -> new ArrayList<>()).add(object);
            }
        }

        final List<T> array = new ArrayList<>(objects);
        Stack<T> stack = new Stack<>();
        boolean[] visited = new boolean[this.objects.size()];
        boolean[] recStack = new boolean[this.objects.size()];

        for (int i = 0; i < array.size(); i++) {
            resolveElement(array.get(i), i, stack, array, visited, recStack, Collections.emptyList());
        }

        this.nodes = new ArrayList<>(this.objects.size());
        while (!stack.empty()) {
            final T element = stack.pop();
            this.nodes.add(element);
        }
        this.nodes = new ArrayList<>(this.nodes);
        return this;
    }

    public boolean isResolved() {
        return this.resolved.get();
    }

    private void resolveElement(
            @NotNull T element,
            int index,
            @NotNull Stack<T> stack,
            @NotNull List<T> array,
            boolean @NotNull [] visited,
            boolean @NotNull [] recStack,
            @NotNull List<T> diagram) {
        if (recStack[index]) {
            this.handleCycle(element, diagram);
            return;
        }

        if (visited[index]) {
            return;
        }

        visited[index] = true;
        recStack[index] = true;

        for (T child : this.edges.getOrDefault(element, Collections.emptyList())) {
            ConditionUtils.is(this.objects.contains(element), "Child " + child + " wasn't an element of this dependency tree");
            int childIndex = array.indexOf(child);

            List<T> newDiagram = new ArrayList<>(diagram);
            newDiagram.add(child);

            resolveElement(child, childIndex, stack, array, visited, recStack, newDiagram);
        }

        recStack[index] = false;

        stack.push(element);
    }

    public CompletableFuture<?> forEachClockwiseAwait(Function<T, CompletableFuture<?>> func) {
        ConditionUtils.is(this.resolved.get(), "The DependencyTree wasn't resolved.");
        Map<T, CompletableFuture<?>> futures = new HashMap<>();

        for (T t : this.nodes) {
            final T[] depends = this.depends(t);
            List<CompletableFuture<?>> dependFutures = new ArrayList<>(depends.length);

            for (T depend : depends) {
                final CompletableFuture<?> future = futures.get(depend);
                dependFutures.add(future);
            }

            final CompletableFuture<?> future = AsyncUtils
                    .allOf(dependFutures)
                    .thenCompose(k -> func.apply(t));
            futures.put(t, future);
        }

        return AsyncUtils.allOf(futures.values());
    }

    public CompletableFuture<?> forEachCounterClockwiseAwait(Function<T, CompletableFuture<?>> func) {
        ConditionUtils.is(this.resolved.get(), "The DependencyTree wasn't resolved.");
        Map<T, CompletableFuture<?>> futures = new HashMap<>();

        this.forEachCounterClockwise(t -> {
            final List<T> child = this.edges.getOrDefault(t, Collections.emptyList());
            List<CompletableFuture<?>> childFutures = new ArrayList<>(child.size());

            for (T depend : child) {
                final CompletableFuture<?> future = futures.get(depend);
                childFutures.add(future);
            }

            final CompletableFuture<?> future = AsyncUtils
                    .allOf(childFutures)
                    .thenCompose(k -> func.apply(t));
            futures.put(t, future);
        });

        return AsyncUtils.allOf(futures.values());
    }

    public void forEachClockwise(Consumer<T> consumer) {
        ConditionUtils.is(this.resolved.get(), "The DependencyTree wasn't resolved.");
        this.nodes.forEach(consumer);
    }

    public void forEachCounterClockwise(Consumer<T> consumer) {
        ConditionUtils.is(this.resolved.get(), "The DependencyTree wasn't resolved.");
        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            consumer.accept(this.nodes.get(i));
        }
    }

}
