package io.fairyproject.container.collection;

import io.fairyproject.container.object.ContainerObj;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ContainerObjCollectorImpl implements ContainerObjCollector {

    private final Set<ContainerObj> objs = ConcurrentHashMap.newKeySet();
    private final Set<Predicate<ContainerObj>> predicates = ConcurrentHashMap.newKeySet();
    private final Set<Consumer<ContainerObj>> addHandlers = ConcurrentHashMap.newKeySet();
    private final Set<Consumer<ContainerObj>> removeHandlers = ConcurrentHashMap.newKeySet();

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
        containerObj.addCollector(this);
    }

    @Override
    public void remove(@NotNull ContainerObj containerObj) {
        this.objs.remove(containerObj);
        this.removeHandlers.forEach(consumer -> consumer.accept(containerObj));
        containerObj.removeCollector(this);
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
