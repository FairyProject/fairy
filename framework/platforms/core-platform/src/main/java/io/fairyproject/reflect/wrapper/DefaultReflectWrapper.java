package io.fairyproject.reflect.wrapper;

import io.fairyproject.util.AccessUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DefaultReflectWrapper implements ReflectWrapper {
    @Override
    public void setField(@NotNull Object instance, @NotNull Field field, @NotNull Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Class<?> findClass(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Field findField(@NotNull Class<?> aClass, @NotNull String fieldName) throws NoSuchFieldException {
        return aClass.getDeclaredField(fieldName);
    }

    @Override
    public Method findMethod(@NotNull Class<?> aClass, @NotNull String methodName, final Class<?>... paramTypes) throws NoSuchMethodException {
        return aClass.getDeclaredMethod(methodName, paramTypes);
    }

    @Override
    public @Nullable Object invokeMethod(@NotNull Object instance, @NotNull Method method, Object... params) {
        try {
            AccessUtil.setAccessible(method);
            return method.invoke(instance, params);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
