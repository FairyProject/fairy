package io.fairyproject.container.collection;

import io.fairyproject.container.object.ContainerObj;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ContainerObjCollectorImpl implements ContainerObjCollector {

    private final Set<ContainerObj> objs = new CopyOnWriteArraySet<>();
    private final Set<Predicate<ContainerObj>> predicates = new HashSet<>();
    private final Set<Consumer<ContainerObj>> addHandlers = new HashSet<>();
    private final Set<Consumer<ContainerObj>> removeHandlers = new HashSet<>();

    @Override
    public ContainerObjCollector withFilter(@NotNull Predicate<ContainerObj> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    @Override
    public ContainerObjCollector withAddHandler(@NotNull Consumer<ContainerObj> consumer) {
        this.addHandlers.add(consumer);
        return this;
    }

    @Override
    public ContainerObjCollector withRemoveHandler(@NotNull Consumer<ContainerObj> consumer) {
        this.removeHandlers.add(consumer);
        return this;
    }

    @Override
    public void add(@NotNull ContainerObj containerObj) {
        this.objs.add(containerObj);
        this.addHandlers.forEach(consumer -> consumer.accept(containerObj));
    }

    @Override
    public boolean remove(@NotNull ContainerObj containerObj) {
        if (this.objs.remove(containerObj)) {
            this.removeHandlers.forEach(consumer -> consumer.accept(containerObj));
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Collection<ContainerObj> all() {
        return Collections.unmodifiableSet(this.objs);
    }

    @NotNull
    @Override
    public Iterator<ContainerObj> iterator() {
        return this.objs.iterator();
    }

    @Override
    public boolean test(ContainerObj containerObj) {
        for (Predicate<ContainerObj> predicate : this.predicates) {
            if (!predicate.test(containerObj))
                return false;
        }
        return true;
    }

}
