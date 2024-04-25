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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class BukkitPluginCacheTest {

    private BukkitPluginCache cache;

    @BeforeEach
    void setUp() {
        this.cache = new BukkitPluginCache();
    }

    @Test
    void addSource() {
        Path path = Paths.get("random");
        this.cache.addSource("test", path);

        assertEquals(path, this.cache.getSource("test"));
    }

    @Test
    void addSourceNullShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> this.cache.addSource(null, Paths.get("random")));
    }

    @Test
    void addSourceNullPathShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> this.cache.addSource("test", null));
    }

    @Test
    void getSourceNull() {
        assertNull(this.cache.getSource("test"));
    }

    @Test
    void getSourceNullShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> this.cache.getSource(null));
    }

    @Test
    void addDependent() {
        this.cache.addDependents("test", Collections.singletonList("test2"));

        assertTrue(this.cache.getDependents("test").contains("test2"));
    }

    @Test
    void addDependentNullShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> this.cache.addDependents(null, Collections.singletonList("test2")));
    }

    @Test
    void addDependentNullDependentsShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> this.cache.addDependents("test", null));
    }

    @Test
    void getDependentEmpty() {
        assertEquals(Collections.emptyList(), this.cache.getDependents("test"));
    }

}