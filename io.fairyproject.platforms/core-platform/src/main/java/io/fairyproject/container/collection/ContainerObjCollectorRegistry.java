package io.fairyproject.container.collection;

import io.fairyproject.container.object.ContainerObj;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerObjCollectorRegistry {

    private final Set<ContainerObjCollector> collectors = ConcurrentHashMap.newKeySet();

    public void add(@NotNull ContainerObjCollector collector) {
        this.collectors.add(collector);
    }

    public boolean remove(@NotNull ContainerObjCollector collector) {
        return this.collectors.remove(collector);
    }

    public void collect(@NotNull ContainerObj obj) {
        this.collectors.stream()
                .filter(collector -> collector.test(obj))
                .forEach(collector -> collector.add(obj));
    }

}
