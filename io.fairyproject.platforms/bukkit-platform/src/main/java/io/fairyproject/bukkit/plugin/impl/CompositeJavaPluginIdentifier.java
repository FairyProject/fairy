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

package io.fairyproject.bukkit.plugin.impl;

import io.fairyproject.bukkit.plugin.JavaPluginIdentifier;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CompositeJavaPluginIdentifier implements JavaPluginIdentifier {

    private final List<JavaPluginIdentifier> identifiers = new ArrayList<>();

    @Override
    public JavaPlugin findByClass(@NotNull Class<?> clazz) {
        for (JavaPluginIdentifier identifier : identifiers) {
            JavaPlugin javaPlugin = identifier.findByClass(clazz);
            if (javaPlugin != null) {
                return javaPlugin;
            }
        }

        return null;
    }

    public void add(JavaPluginIdentifier javaPluginIdentifier) {
        identifiers.add(javaPluginIdentifier);
    }

    public void addFirst(JavaPluginIdentifier javaPluginIdentifier) {
        identifiers.add(0, javaPluginIdentifier);
    }

    @ApiStatus.Internal
    public void clear() {
        identifiers.clear();
    }
}
