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

package io.fairyproject.plugin.initializer;

import io.fairyproject.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class DefaultPluginClassInitializer implements PluginClassInitializer {
    @Override
    public @NotNull ClassLoader initializeClassLoader(@NotNull ClassLoader classLoader) {
        return classLoader;
    }

    @Override
    public Plugin create(String mainClassPath, ClassLoader classLoader) {
        Class<?> mainClass;
        try {
            mainClass = classLoader.loadClass(mainClassPath);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load mainClass " + mainClassPath, e);
        }

        if (!Plugin.class.isAssignableFrom(mainClass)) {
            throw new IllegalStateException(String.format("%s wasn't implementing Plugin", mainClass));
        }

        try {
            return (Plugin) mainClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("Failed to new instance " + mainClassPath + " (Does it has no args constructor in the class?)", e);
        }
    }
}
