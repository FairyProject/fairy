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

package io.fairyproject.devtools.bukkit;

import io.fairyproject.util.ConditionUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitPluginCache {

    private final Map<String, Path> sources = new HashMap<>();
    private final Map<String, List<String>> dependents = new HashMap<>();

    public void addSource(String name, Path file) {
        ConditionUtils.notNull(name, "name");
        ConditionUtils.notNull(file, "file");

        this.sources.put(name, file);
    }

    public Path getSource(String name) {
        ConditionUtils.notNull(name, "name");

        return this.sources.get(name);
    }

    public void addDependents(String name, List<String> depends) {
        ConditionUtils.notNull(name, "name");
        ConditionUtils.notNull(depends, "depends");

        this.dependents.put(name, depends);
    }

    public List<String> getDependents(String name) {
        return this.dependents.getOrDefault(name, Collections.emptyList());
    }

}
