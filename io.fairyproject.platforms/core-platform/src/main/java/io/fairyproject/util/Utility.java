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

package io.fairyproject.util;

import io.fairyproject.task.Task;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utility {

    public static <T> CompletableFuture<Void> forEachSlowly(Consumer<T> consumer, Collection<? extends T> collection) {
        List<T> list = new ArrayList<>(collection);
        int size = collection.size();
        int diff = (int) Math.ceil(collection.size() / 20.0);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (int i = 0, ticks = 0; i < size; i += diff) {
            int start = i;
            int end = i + diff;
            CompletableFuture<Void> future = new CompletableFuture<>();
            Task.runMainLater(() -> {
                for (int i1 = start; i1 < end; ++i1) {
                    if (i1 >= list.size()) {
                        break;
                    }

                    consumer.accept(list.get(i1));
                }
                future.complete(null);
            }, ++ticks);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static <T> Constructor<T> getConstructor(Class<T> parentClass, Class<?>... parameterTypes) {
        try {
            return parentClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static <T> String joinToString(final T[] array) {
        return array == null ? "null" : joinToString(Arrays.asList(array));
    }

    public static <T> String joinToString(final T[] array, final String delimiter) {
        return array == null ? "null" : joinToString(Arrays.asList(array), delimiter);
    }

    public static <T> String joinToString(final Iterable<T> array) {
        return array == null ? "null" : joinToString(array, ", ");
    }

    public static <T> String joinToString(final Iterable<T> array, final String delimiter) {
        return join(array, delimiter, object -> object == null ? "" : object.toString());
    }

    public static <T> String join(final Iterable<T> array, final String delimiter, final Stringer<T> stringer) {
        final Iterator<T> it = array.iterator();
        StringBuilder message = new StringBuilder();

        while (it.hasNext()) {
            final T next = it.next();

            if (next != null)
                message.append(stringer.toString(next)).append(it.hasNext() ? delimiter : "");
        }

        return message.toString();
    }

    public static <T> List<Field> getAllFields(Class clazz) {
        List<Class> classes = new ArrayList<>();
        while (clazz != Object.class) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }

        Collections.reverse(classes);
        return classes.stream()
                .map(Class::getDeclaredFields)
                .flatMap(Stream::of)
                .collect(Collectors.toList());
    }

    public static Set<Class<?>> getSuperClasses(Class<?> type) {
        Set<Class<?>> superclasses = new HashSet<>();

        Class<?> superclass = type;
        while (superclass != null && superclass != Object.class) {
            superclasses.add(superclass);

            superclass = superclass.getSuperclass();
        }

        return superclasses;
    }

    public static Collection<Class<?>> getSuperAndInterfaces(Class<?> type) {
        Set<Class<?>> superclasses = getSuperClasses(type);
        Set<Class<?>> result = new HashSet<>(superclasses);

        while (superclasses.size() > 0) {
            List<Class<?>> clone = new ArrayList<>(superclasses);
            superclasses.clear();

            for (Class<?> superclass : clone) {
                result.add(superclass);

                Class<?>[] interfaces = superclass.getInterfaces();
                if (interfaces.length > 0) {
                    superclasses.addAll(Arrays.asList(interfaces));
                }
            }
        }

        return result;
    }

    public interface Stringer<T> {

        /**
         * Convert the given object into a string
         *
         * @param object
         * @return
         */
        String toString(T object);
    }

    public static <T> Class<T> wrapPrimitive(Class<T> c) {
        return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
            = new ConcurrentHashMap<>();

    static {
        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
        PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
    }

    /**
     * Returns the name of the class, as the JVM would output it. For instance, for an int, "I" is returned, for an
     * array of Objects, "[Ljava/lang/Object;" is returned. If the input is null, null is returned.
     *
     * @param clazz
     * @return
     */
    public static String getJVMName(Class clazz) {
        if (clazz == null) {
            return null;
        }
        //For arrays, .getName() is fine.
        if (clazz.isArray()) {
            return clazz.getName().replace('.', '/');
        }
        if (clazz == boolean.class) {
            return "Z";
        } else if (clazz == byte.class) {
            return "B";
        } else if (clazz == short.class) {
            return "S";
        } else if (clazz == int.class) {
            return "I";
        } else if (clazz == long.class) {
            return "J";
        } else if (clazz == float.class) {
            return "F";
        } else if (clazz == double.class) {
            return "D";
        } else if (clazz == char.class) {
            return "C";
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
    }

    /**
     * Generically and dynamically returns the array class type for the given class type. The dynamic equivalent of
     * sending {@code String.class} and getting {@code String[].class}. Works with array types as well.
     *
     * @param clazz The class to convert to an array type.
     * @return The array type of the input class.
     */
    public static Class<?> getArrayClassFromType(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        try {
            return Class.forName("[" + getJVMName(clazz).replace('/', '.'));
        } catch (ClassNotFoundException ex) {
            // This cannot naturally happen, as we are simply creating an array type for a real type that has
            // clearly already been loaded.
            throw new NoClassDefFoundError(ex.getMessage());
        }
    }

    public static Type[] getGenericTypes(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments();
        }
        return null;
    }

    public static boolean isParametersEquals(Type[] parametersA, Type[] parametersB) {
        boolean equal = true;
        if (parametersA.length != parametersB.length) {
            return false;
        }

        for (int i = 0; i < parametersA.length; i++) {
            Type typeA = parametersA[i];
            if (typeA instanceof Class) {
                typeA = Utility.wrapPrimitive((Class<?>) typeA);
            }

            Type typeB = parametersB[i];
            if (typeB instanceof Class) {
                typeB = Utility.wrapPrimitive((Class<?>) typeB);
            }
            if (typeA != typeB) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    public static <I, R> R[] toArrayType(I[] originalArray, Class<R> resultType, Function<I, R> transfer) {
        R[] result = (R[]) Array.newInstance(resultType, originalArray.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = transfer.apply(originalArray[i]);
        }
        return result;
    }

    public static <T> void twice(Collection<T> collection, BiConsumer<T, T> consumer) {
        for (T t1 : collection) {
            for (T t2 : collection) {
                consumer.accept(t1, t2);
            }
        }
    }

}
