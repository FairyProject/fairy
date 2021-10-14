package io.fairyproject.bootstrap.util;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Path;

@UtilityClass
public class ClassLoaderUtil {

    public void addURLToClassLoader(Path path) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Unsafe unsafe;
        MethodHandles.Lookup lookup;

        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            unsafe.ensureClassInitialized(MethodHandles.Lookup.class);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable t) {
            throw new IllegalStateException("Unsafe not found");
        }

        Field field;

        try {
            field = classLoader.getClass().getDeclaredField("ucp");
        } catch (NoSuchFieldException e) {
            if (classLoader instanceof URLClassLoader) {
                try {
                    field = URLClassLoader.class.getDeclaredField("ucp");
                } catch (NoSuchFieldException e2) {
                    throw new RuntimeException("Couldn't find ucp field from ClassLoader!");
                }
            } else {
                try {
                    final Class<?> jdkClassLoader = Class.forName("jdk.internal.loader.BuiltinClassLoader");

                    if (jdkClassLoader.isInstance(classLoader)) {
                        field = jdkClassLoader.getDeclaredField("ucp");
                    } else {
                        throw new RuntimeException("Couldn't find ucp field from ClassLoader!");
                    }
                } catch (ClassNotFoundException | NoSuchFieldException classNotFoundException) {
                    throw new RuntimeException("Couldn't find ucp field from ClassLoader!");
                }
            }
        }

        try {
            long ucpOffset = unsafe.objectFieldOffset(field);
            Object ucp = unsafe.getObject(classLoader, ucpOffset);
            MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
            methodHandle.invoke(ucp, path.toUri().toURL());
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while adding spigot.jar to class path!", throwable);
        }
    }

}
