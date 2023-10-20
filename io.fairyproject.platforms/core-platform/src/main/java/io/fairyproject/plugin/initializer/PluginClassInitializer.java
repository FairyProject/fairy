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

/**
 * Represents a class initializer for a plugin.
 * It is used to initialize main class of the plugin and possibility to replace the class loader.
 */
public interface PluginClassInitializer {

    /**
     * Initialize the class loader.
     * This method is called before the main class is initialized.
     *
     * @param name the name of the plugin
     * @param classLoader the current class loader
     * @return the new class loader, return the same class loader if you don't want to replace it
     */
    @NotNull ClassLoader initializeClassLoader(@NotNull String name, @NotNull ClassLoader classLoader);

    /**
     * Create a new instance of the main class.
     *
     * @param mainClassPath the main class path
     * @return the new instance of the main class
     */
    Plugin create(String mainClassPath, ClassLoader classLoader);

}
