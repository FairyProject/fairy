/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.devtools.watcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

class ClasspathFileWatcherTest {

    private Path directory;
    private ClasspathFileWatcher classpathFileWatcher;
    private FileAlterationListener listener;

    @BeforeEach
    void setUp() throws IOException {
        directory = Files.createTempDirectory("test");
        Files.createFile(directory.resolve("test.txt"));

        listener = Mockito.mock(FileAlterationListener.class);

        classpathFileWatcher = new ClasspathFileWatcher(10L);
    }

    @AfterEach
    void tearDown() throws Exception {
        FileUtils.deleteDirectory(directory.toFile());

        if (classpathFileWatcher.isStarted())
            classpathFileWatcher.stop();
    }

    @Test
    void addURLShouldCreateObserver() throws IOException, URISyntaxException {
        Path path = Files.createTempDirectory("test-2");

        classpathFileWatcher.addURL(path.toUri().toURL(), listener);

        Assertions.assertEquals(1, StreamSupport
                .stream(classpathFileWatcher.getObservers().spliterator(), false)
                .count());
    }

    @Test
    void removeURLShouldRemoveObserver() throws IOException, URISyntaxException {
        Path path = Files.createTempDirectory("test-2");

        classpathFileWatcher.addURL(path.toUri().toURL(), listener);
        classpathFileWatcher.removeURL(path.toUri().toURL());

        Assertions.assertEquals(0, StreamSupport
                .stream(classpathFileWatcher.getObservers().spliterator(), false)
                .count());
    }

    @Nested
    class Monitoring {

        @BeforeEach
        void setUp() throws Exception {
            classpathFileWatcher.addURL(directory.toUri().toURL(), listener);
            classpathFileWatcher.start();
        }

        @Test
        void startShouldBeStarted() {
            Assertions.assertTrue(classpathFileWatcher.isStarted());
        }

        @Test
        void createFileShouldCallListener() throws Exception {
            Path path = directory.resolve("test-2.txt");
            Files.createFile(path);

            Thread.sleep(100L);

            Mockito.verify(listener).onFileCreate(path.toFile());
        }

        @Test
        void deleteFileShouldCallListener() throws Exception {
            Path path = directory.resolve("test.txt");
            Files.delete(path);

            Thread.sleep(100L);

            Mockito.verify(listener).onFileDelete(path.toFile());
        }

        @Test
        void changeFileShouldCallListener() throws Exception {
            Path path = directory.resolve("test.txt");
            Files.write(path, "test".getBytes());

            Thread.sleep(100L);

            Mockito.verify(listener).onFileChange(path.toFile());
        }

    }

}