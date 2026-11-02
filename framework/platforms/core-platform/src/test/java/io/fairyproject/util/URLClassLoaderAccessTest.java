package io.fairyproject.util;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
