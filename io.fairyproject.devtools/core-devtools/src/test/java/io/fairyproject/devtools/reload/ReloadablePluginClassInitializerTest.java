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

package io.fairyproject.devtools.reload;

import io.fairyproject.devtools.reload.classloader.ReloadableClassLoader;
import io.fairyproject.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ReloadablePluginClassInitializerTest {

    private URL url;
    private ReloadableClassLoader testClassLoader;
    private ReloadablePluginClassInitializer reloadablePluginClassInitializer;

    @BeforeEach
    void setUp() throws ClassNotFoundException {
        this.testClassLoader = Mockito.mock(ReloadableClassLoader.class);
        this.url = this.getClass().getProtectionDomain().getCodeSource().getLocation();

        ClasspathCollection classpathCollection = new ClasspathCollection();
        classpathCollection.addURL("plugin1", url);
        Mockito.doReturn(TestPlugin.class).when(testClassLoader).loadClass("io.fairyproject.devtools.reload.TestPlugin");

        this.reloadablePluginClassInitializer = new ReloadablePluginClassInitializer(classpathCollection);
    }

    @Test
    void initializeClassLoader() {
        ClassLoader classLoader = reloadablePluginClassInitializer.initializeClassLoader("plugin1", testClassLoader);

        assertTrue(classLoader instanceof ReloadableClassLoader);
        assertSame(classLoader.getParent(), testClassLoader);
        assertSame(((ReloadableClassLoader) classLoader).getURLs()[0], url);
    }

    @Test
    void initializeClassLoaderWithUnknownNameMustReturnSameClassLoader() {
        ClassLoader classLoader = reloadablePluginClassInitializer.initializeClassLoader("plugin2", testClassLoader);

        assertSame(classLoader, testClassLoader);
    }

    @Test
    void createMustSetPluginClassLoader() {
        Plugin plugin = reloadablePluginClassInitializer.create("io.fairyproject.devtools.reload.TestPlugin", testClassLoader);

        Mockito.verify(testClassLoader).setPlugin(plugin);
    }

    @Test
    void createWithNoArgConstructorShouldBeEmpty() {
        ReloadablePluginClassInitializer reloadablePluginClassInitializer = new ReloadablePluginClassInitializer();
        ClasspathCollection classpathCollection = reloadablePluginClassInitializer.getClasspathCollection();

        assertEquals(0, classpathCollection.getURLs().length);
    }

    @Test
    void createWithNoArgConstructorMustReadProperty() {
        System.setProperty("io.fairyproject.devtools.classpath", "plugin1|" + url.getPath());

        ReloadablePluginClassInitializer reloadablePluginClassInitializer = new ReloadablePluginClassInitializer();
        ClasspathCollection classpathCollection = reloadablePluginClassInitializer.getClasspathCollection();

        assertTrue(classpathCollection.getURLs().length > 0);
        assertEquals(url.getPath(), classpathCollection.getURLs()[0].getPath());
    }

}