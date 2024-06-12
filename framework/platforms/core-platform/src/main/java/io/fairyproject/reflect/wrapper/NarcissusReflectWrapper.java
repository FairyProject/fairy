package io.fairyproject.reflect.wrapper;

//import io.github.toolfactory.narcissus.Narcissus;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//public class NarcissusReflectWrapper implements ReflectWrapper {
//    @Override
//    public void setField(@NotNull Object instance, @NotNull Field field, @NotNull Object value) {
//         Narcissus.setField(instance, field, value);
//    }
//
//    @Override
//    public Class<?> findClass(@NotNull String name) {
//        return Narcissus.findClass(name);
//    }
//
//    @Override
//    public Field findField(@NotNull Class<?> aClass, @NotNull String fieldName) throws NoSuchFieldException {
//        return Narcissus.findField(aClass, fieldName);
//    }
//
//    @Override
//    public Method findMethod(@NotNull Class<?> aClass, @NotNull String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
//        return Narcissus.findMethod(aClass, methodName, paramTypes);
//    }
//
//    @Override
//    public @Nullable Object invokeMethod(@NotNull Object instance, @NotNull Method method, Object... params) {
//        return Narcissus.invokeMethod(instance, method, params);
//    }
//}
