package io.fairyproject.container.collection;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.ConditionUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ContainerObjCollector extends Iterable<ContainerObj>, Predicate<ContainerObj> {

    static ContainerObjCollector create() {
        return new ContainerObjCollectorImpl();
    }

    static Predicate<ContainerObj> inherits(Class<?> classInherit) {
        return containerObj -> classInherit.isAssignableFrom(containerObj.type());
    }

    static <T> Consumer<ContainerObj> warpInstance(Class<T> type, Consumer<T> consumer) {
        return containerObj -> {
            final Object instance = containerObj.instance();
            ConditionUtils.notNull(instance, "The instance of the container object hasn't been constructed.");
            ConditionUtils.check(type.isAssignableFrom(containerObj.type()), String.format("The container object type %s doesn't match with %s", containerObj.type(), type));

            consumer.accept(type.cast(instance));
        };
    }

    @Contract("_ -> this")
    ContainerObjCollector withFilter(@NotNull Predicate<ContainerObj> predicate);

    @Contract("_ -> this")
    ContainerObjCollector withAddHandler(@NotNull Consumer<ContainerObj> consumer);

    @Contract("_ -> this")
    ContainerObjCollector withRemoveHandler(@NotNull Consumer<ContainerObj> consumer);

    void add(@NotNull ContainerObj containerObj);

    void remove(@NotNull ContainerObj containerObj);

    @NotNull Collection<ContainerObj> all();

}
