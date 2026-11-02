package io.fairyproject.util;

import io.fairyproject.Debug;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.github.toolfactory.jvm.Driver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Provides access to {@link URLClassLoader}#addURL.
 */
public abstract class URLClassLoaderAccess {

    private final URLClassLoader classLoader;

    protected URLClassLoaderAccess(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Creates a {@link URLClassLoaderAccess} for the given class loader.
     *
     * @param classLoader the class loader
     * @return the access object
     */
    public static URLClassLoaderAccess create(URLClassLoader classLoader) {
        if (classLoader == null)
            return Noop.INSTANCE;

        if (Reflection.isSupported()) {
            Debug.log("Using Reflection URL class loader access");
            return new Reflection(classLoader);
        }

        URLClassLoaderAccess access = URLClassLoaderAccess.tryCreate("JVM driver", () -> new JvmDriver(classLoader));
        if (access != null)
            return access;

        access = URLClassLoaderAccess.tryCreate("Unsafe", () -> new Unsafe(classLoader));
        if (access != null)
            return access;

        Debug.log("Using NoOp URL class loader access");
        return Noop.INSTANCE;
    }

    private static URLClassLoaderAccess tryCreate(String name, Supplier<URLClassLoaderAccess> supplier) {
        try {
            URLClassLoaderAccess access = supplier.get();
            Debug.log("Using %s URL class loader access", name);
            return access;
        } catch (Throwable throwable) {
            Debug.warn("%s URL class loader access is unavailable: %s", name, throwable.toString());
            return null;
        }
    }

    /**
     * Adds the given URL to the class loader.
     *
     * @param url the URL to add
     */
    public abstract void addURL(@NotNull URL url);

    public void addPath(@NotNull Path path) {
        ThrowingRunnable.sneaky(() -> this.addURL(path.toUri().toURL())).run();
    }

    /**
     * Accesses using reflection, not supported on Java 9+.
     */
    private static class Reflection extends URLClassLoaderAccess {
        private static final Method ADD_URL_METHOD;

        static {
            Method addUrlMethod;
            try {
                addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
            } catch (Throwable e) {
                addUrlMethod = null;
            }
            ADD_URL_METHOD = addUrlMethod;
        }

        private static boolean isSupported() {
            return ADD_URL_METHOD != null;
        }

        Reflection(URLClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        public void addURL(@NotNull URL url) {
            try {
                ADD_URL_METHOD.invoke(super.classLoader, url);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class JvmDriver extends URLClassLoaderAccess {

        private final Driver driver;
        private final Collection<URL> unopenedURLs;
        private final Collection<URL> pathURLs;

        @SuppressWarnings("unchecked")
        protected JvmDriver(URLClassLoader classLoader) {
            super(classLoader);

            try {
                this.driver = Driver.Factory.getNew();
                Object ucp = this.fetchField(URLClassLoader.class, classLoader, "ucp");
                this.unopenedURLs = (Collection<URL>) this.fetchField(ucp.getClass(), ucp, "unopenedUrls");
                this.pathURLs = (Collection<URL>) this.fetchField(ucp.getClass(), ucp, "path");
            } catch (Throwable e) {
                throw new IllegalStateException("Unable to access URLClassLoader internals through jvm-driver.", e);
            }
        }

        private Object fetchField(final Class<?> clazz, final Object object, final String name) throws NoSuchFieldException {
            for (Field field : this.driver.getDeclaredFields(clazz)) {
                if (field.getName().equals(name)) {
                    return this.driver.getFieldValue(object, field);
                }
            }
            throw new NoSuchFieldException(name);
        }

        @Override
        public void addURL(@NotNull URL url) {
            this.unopenedURLs.add(url);
            this.pathURLs.add(url);
        }
    }

    /**
     * Accesses using sun.misc.Unsafe, supported on Java 9+.
     *
     * @author Vaishnav Anil (https://github.com/slimjar/slimjar)
     */
    private static class Unsafe extends URLClassLoaderAccess {
        private static final Object UNSAFE;
        private static final Method OBJECT_FIELD_OFFSET_METHOD;
        private static final Method GET_OBJECT_METHOD;

        static {
            Object unsafe;
            Method objectFieldOffsetMethod;
            Method getObjectMethod;
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = unsafeField.get(null);
                objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
                getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
            } catch (Throwable t) {
                unsafe = null;
                objectFieldOffsetMethod = null;
                getObjectMethod = null;
            }
            UNSAFE = unsafe;
            OBJECT_FIELD_OFFSET_METHOD = objectFieldOffsetMethod;
            GET_OBJECT_METHOD = getObjectMethod;
        }

        private final Collection<URL> unopenedURLs;
        private final Collection<URL> pathURLs;

        @SuppressWarnings("unchecked")
        Unsafe(URLClassLoader classLoader) {
            super(classLoader);

            if (UNSAFE == null)
                throw new IllegalStateException("sun.misc.Unsafe is not available.");

            try {
                Object ucp = fetchField(URLClassLoader.class, classLoader, "ucp");
                this.unopenedURLs = (Collection<URL>) fetchField(ucp.getClass(), ucp, "unopenedUrls");
                this.pathURLs = (Collection<URL>) fetchField(ucp.getClass(), ucp, "path");
            } catch (Throwable e) {
                throw new IllegalStateException("Unable to access URLClassLoader internals through Unsafe.", e);
            }
        }

        private static Object fetchField(final Class<?> clazz, final Object object, final String name) throws NoSuchFieldException {
            Field field = clazz.getDeclaredField(name);
            try {
                long offset = (long) OBJECT_FIELD_OFFSET_METHOD.invoke(UNSAFE, field);
                return GET_OBJECT_METHOD.invoke(UNSAFE, object, offset);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to read field through Unsafe.", e);
            }
        }

        @Override
        public void addURL(@NotNull URL url) {
            this.unopenedURLs.add(url);
            this.pathURLs.add(url);
        }
    }

    private static class Noop extends URLClassLoaderAccess {
        private static final Noop INSTANCE = new Noop();

        private Noop() {
            super(null);
        }

        @Override
        public void addURL(@NotNull URL url) {
            throw new UnsupportedOperationException();
        }
    }

}
