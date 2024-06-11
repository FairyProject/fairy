/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.reflect;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

@UtilityClass
public class Reflect {

    public static <T, A extends Annotation> T getAnnotationValue(Class<?> annotatedClass, Class<A> annotation, AnnotationValueFunction<A, T> function) {
        final A a = annotatedClass.getAnnotation(annotation);
        return function.apply(a);
    }

    public static <T, A extends Annotation> T getAnnotationValueOrNull(Class<?> annotatedClass, Class<A> annotation, Function<A, T> function) {
        final A a = annotatedClass.getAnnotation(annotation);
        if (a == null)
            return null;
        return function.apply(a);
    }

    public static <T, A extends Annotation> T getAnnotationValueOrThrow(Class<?> annotatedClass, Class<A> annotation, Function<A, T> function) {
        final A a = annotatedClass.getAnnotation(annotation);
        if (a == null) {
            throw new IllegalArgumentException("Couldn't find annotation " + annotation + " on " + annotatedClass + ".");
        }
        return function.apply(a);
    }

    public interface AnnotationValueFunction<A extends Annotation, T> extends Function<A, T> {

        @Override
        @NonNull T apply(@Nullable A input);
    }

    public static <T> Class<T> getParameter(Field field, int index) {
        final Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("The field " + field + " is not parameterized!");
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        if (parameterizedType.getActualTypeArguments().length <= index) {
            throw new ArrayIndexOutOfBoundsException("Requested parameter index: " + index + ", Actual length: " + parameterizedType.getActualTypeArguments().length);
        }

        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public static Optional<Class<?>> getCallerClass(int depth) {
        return Optional.ofNullable(CallerClass.impl.getCallerClass(depth + 1));
    }

    public static Class<?> getCallerClassNotOptional(int depth) {
        return CallerClass.impl.getCallerClass(depth);
    }

    public static Field setAccessible(Field field) throws ReflectiveOperationException {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        int modifiers = field.getModifiers();
        if (!Modifier.isFinal(modifiers)) {
            return field;
        }
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            if ("modifiers".equals(e.getMessage()) || (e.getCause() != null && e.getCause().getMessage() != null &&  e.getCause().getMessage().equals("modifiers"))) {
                // https://github.com/ViaVersion/ViaVersion/blob/e07c994ddc50e00b53b728d08ab044e66c35c30f/bungee/src/main/java/us/myles/ViaVersion/bungee/platform/BungeeViaInjector.java
                // Java 12 compatibility *this is fine*
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                for (Field classField : fields) {
                    if ("modifiers".equals(classField.getName())) {
                        classField.setAccessible(true);
                        classField.set(field, modifiers & ~Modifier.FINAL);
                        break;
                    }
                }
            } else {
                throw e;
            }
        }
        return field;
    }

    public static Method setAccessible(Method method) throws ReflectiveOperationException {
        if (method.isAccessible()) {
            return method;
        }

        method.setAccessible(true);
        return method;
    }

    public static Constructor setAccessible(Constructor constructor) throws ReflectiveOperationException {
        if (constructor.isAccessible()) {
            return constructor;
        }

        constructor.setAccessible(true);
        return constructor;
    }

    private static abstract class CallerClass {

        private static CallerClass impl;

        static {
//            try {
//                Class.forName("sun.reflect.Reflection");
//                impl = new ReflectionImpl();
//            } catch (ClassNotFoundException e) {
//                impl = new StackTraceImpl();
//            }
            impl = new StackTraceImpl();
        }

        abstract Class<?> getCallerClass(int i);

        /**
         * Removed on Java 11
         */
        private static class ReflectionImpl extends CallerClass {

            @SuppressWarnings({"deprecation", "restriction"})
            @Override
            Class<?> getCallerClass(int i) {
//                return Reflection.getCallerClass(i);
                return null;
            }
        }

        private static class StackTraceImpl extends CallerClass {

            @Override
            Class<?> getCallerClass(int i) {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                String className = elements[i].getClassName();
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException ignored) {
                }
                try {
                    return Class.forName(className);
                } catch (NullPointerException | ClassNotFoundException ignored) {
                }
                return null;
            }
        }
    }

}
