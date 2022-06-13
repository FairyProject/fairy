package io.fairyproject.container;

import io.fairyproject.container.object.ContainerObj;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ContainerRef {

    private static final ClassValue<ContainerRef> GLOBAL = new ClassValue<ContainerRef>() {
        @Override
        protected ContainerRef computeValue(Class<?> aClass) {
            return new ContainerRef();
        }
    };

    private ContainerObj obj;

    public static boolean hasObj(@NotNull Class<?> objectType) {
        return getObj(objectType) != null;
    }
    public static @Nullable ContainerObj getObj(@NotNull Class<?> objectType) {
        return GLOBAL.get(objectType).get();
    }

    public static void setObj(@NotNull Class<?> objectType, @Nullable ContainerObj obj) {
        GLOBAL.get(objectType).set(obj);
    }

    public ContainerObj get() {
        return this.obj;
    }

    public void set(ContainerObj obj) {
        this.obj = obj;
    }

}
