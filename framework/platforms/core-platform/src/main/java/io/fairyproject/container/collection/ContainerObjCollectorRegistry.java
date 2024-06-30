package io.fairyproject.container.collection;

import io.fairyproject.container.object.ContainerObj;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ContainerObjCollectorRegistry {

    private final Set<ContainerObjCollector> collectors = new CopyOnWriteArraySet<>();

    public void add(@NotNull ContainerObjCollector collector) {
        this.collectors.add(collector);
    }

    public boolean remove(@NotNull ContainerObjCollector collector) {
        return this.collectors.remove(collector);
    }

    public void addToCollectors(@NotNull ContainerObj obj) {
        this.collectors.stream()
                .filter(collector -> collector.test(obj))
                .forEach(collector -> collector.add(obj));
    }

    public void removeFromCollectors(@NotNull ContainerObj obj) {
        this.collectors.forEach(collector -> collector.remove(obj));
    }

}
