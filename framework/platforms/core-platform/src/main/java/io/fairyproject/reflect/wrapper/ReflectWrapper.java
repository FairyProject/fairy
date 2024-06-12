package io.fairyproject.reflect.wrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ReflectWrapper {

    static ReflectWrapper get() {
        return Companion.INSTANCE;
    }

    void setField(@NotNull Object instance, @NotNull Field field, @NotNull Object value);

    Class<?> findClass(@NotNull String name);

    Field findField(@NotNull Class<?> aClass, @NotNull String fieldName) throws NoSuchFieldException;

    @Nullable
    Object invokeMethod(@NotNull Object instance, @NotNull Method method, Object... params);

    Method findMethod(@NotNull Class<?> aClass, @NotNull String methodName, final Class<?>... paramTypes) throws NoSuchMethodException;

    class Companion {

        public static ReflectWrapper INSTANCE;

        static {
//            if (Narcissus.libraryLoaded) {
//                INSTANCE = new NarcissusReflectWrapper();
//            } else {
                INSTANCE = new DefaultReflectWrapper();
//            }
        }

    }

}
