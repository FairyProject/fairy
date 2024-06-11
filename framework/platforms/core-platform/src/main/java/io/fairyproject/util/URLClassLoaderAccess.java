package io.fairyproject.util;

import io.fairyproject.Debug;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.github.toolfactory.narcissus.Narcissus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;

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
        } else if (NarcissusUnsafe.isSupported()) {
            Debug.log("Using Narcissus Unsafe URL class loader access");
            return new NarcissusUnsafe(classLoader);
        } else {
            Debug.log("Using NoOp URL class loader access");
            return Noop.INSTANCE;
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
            } catch (Exception e) {
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

    private static class NarcissusUnsafe extends URLClassLoaderAccess {

        private final Collection<URL> unopenedURLs;
        private final Collection<URL> pathURLs;

        protected NarcissusUnsafe(URLClassLoader classLoader) {
            super(classLoader);

            Collection<URL> unopenedURLs;
            Collection<URL> pathURLs;
            try {
                Object ucp = NarcissusUnsafe.fetchField(URLClassLoader.class, classLoader, "ucp");
                unopenedURLs = (Collection<URL>) NarcissusUnsafe.fetchField(ucp.getClass(), ucp, "unopenedUrls");
                pathURLs = (Collection<URL>) NarcissusUnsafe.fetchField(ucp.getClass(), ucp, "path");
            } catch (Throwable e) {
                unopenedURLs = null;
                pathURLs = null;
            }
            this.unopenedURLs = unopenedURLs;
            this.pathURLs = pathURLs;
        }

        public static boolean isSupported() {
            return Narcissus.libraryLoaded;
        }

        private static Object fetchField(final Class<?> clazz, final Object object, final String name) throws NoSuchFieldException {
            Field field = Narcissus.findField(clazz, name);
            return Narcissus.getField(object, field);
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
//    private static class Unsafe extends URLClassLoaderAccess {
//        private static final sun.misc.Unsafe UNSAFE;
//
//        static {
//            sun.misc.Unsafe unsafe;
//            try {
//                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
//                unsafeField.setAccessible(true);
//                unsafe = (sun.misc.Unsafe) unsafeField.get(null);
//            } catch (Throwable t) {
//                unsafe = null;
//            }
//            UNSAFE = unsafe;
//        }
//
//        private static boolean isSupported() {
//            return UNSAFE != null;
//        }
//
//        private final Collection<URL> unopenedURLs;
//        private final Collection<URL> pathURLs;
//
//        @SuppressWarnings("unchecked")
//        Unsafe(URLClassLoader classLoader) {
//            super(classLoader);
//
//            Collection<URL> unopenedURLs;
//            Collection<URL> pathURLs;
//            try {
//                Object ucp = fetchField(URLClassLoader.class, classLoader, "ucp");
//                unopenedURLs = (Collection<URL>) fetchField(ucp.getClass(), ucp, "unopenedUrls");
//                pathURLs = (Collection<URL>) fetchField(ucp.getClass(), ucp, "path");
//            } catch (Throwable e) {
//                unopenedURLs = null;
//                pathURLs = null;
//            }
//            this.unopenedURLs = unopenedURLs;
//            this.pathURLs = pathURLs;
//        }
//
//        private static Object fetchField(final Class<?> clazz, final Object object, final String name) throws NoSuchFieldException {
//            Field field = clazz.getDeclaredField(name);
//            long offset = UNSAFE.objectFieldOffset(field);
//            return UNSAFE.getObject(object, offset);
//        }
//
//        @Override
//        public void addURL(@NotNull URL url) {
//            this.unopenedURLs.add(url);
//            this.pathURLs.add(url);
//        }
//    }

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