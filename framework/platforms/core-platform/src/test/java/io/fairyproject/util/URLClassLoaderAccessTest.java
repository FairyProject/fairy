package io.fairyproject.util;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URLClassLoaderAccessTest {

    @Test
    void createShouldNotFailForUrlClassLoader() throws Exception {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[0], null)) {
            assertDoesNotThrow(() -> URLClassLoaderAccess.create(classLoader));
        }
    }

    @Test
    void addPathShouldExtendUrlClassLoaderClasspath() throws Exception {
        Path path = Files.createTempDirectory("url-class-loader-access-test");
        URL url = path.toUri().toURL();

        try (URLClassLoader classLoader = new URLClassLoader(new URL[0], null)) {
            URLClassLoaderAccess access = URLClassLoaderAccess.create(classLoader);
            access.addPath(path);

            assertTrue(Arrays.asList(classLoader.getURLs()).contains(url));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void addPathShouldMakeResourceReadable() throws Exception {
        Path directory = Files.createTempDirectory("url-class-loader-resource-test");
        Path resource = directory.resolve("class-loader-marker.txt");
        byte[] content = "loaded".getBytes(StandardCharsets.UTF_8);
        Files.write(resource, content);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[0], null)) {
            assertNull(classLoader.getResource("class-loader-marker.txt"));

            URLClassLoaderAccess.create(classLoader).addPath(directory);

            try (InputStream input = classLoader.getResourceAsStream("class-loader-marker.txt")) {
                assertNotNull(input);
                assertEquals("loaded", new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).readLine());
            }
        } finally {
            Files.deleteIfExists(resource);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void addPathShouldMakeClassLoadable() throws Exception {
        Path directory = Files.createTempDirectory("url-class-loader-class-test");
        String className = LoadableFixture.class.getName();
        String resourceName = className.replace('.', '/') + ".class";
        Path classFile = directory.resolve(resourceName);
        Files.createDirectories(classFile.getParent());
        try (InputStream input = getClass().getResourceAsStream("/" + resourceName)) {
            assertNotNull(input);
            Files.copy(input, classFile);
        }

        try (URLClassLoader classLoader = new URLClassLoader(new URL[0], null)) {
            assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(className));

            URLClassLoaderAccess.create(classLoader).addPath(directory);

            Class<?> loadedClass = classLoader.loadClass(className);
            assertEquals(classLoader, loadedClass.getClassLoader());
            assertEquals("loaded", loadedClass.getMethod("value").invoke(null));
        } finally {
            Files.deleteIfExists(classFile);
            for (Path path = classFile.getParent(); path != null && path.startsWith(directory); path = path.getParent()) {
                Files.deleteIfExists(path);
            }
        }
    }

    public static class LoadableFixture {
        public static String value() {
            return "loaded";
        }
    }
}
