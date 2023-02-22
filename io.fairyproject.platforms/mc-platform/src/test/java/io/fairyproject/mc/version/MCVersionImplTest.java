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

package io.fairyproject.mc.version;

import io.fairyproject.mc.version.impl.MCVersionImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MCVersionImplTest {

    @Test
    public void compareTo_shouldReturnCorrectValues() {
        MCVersionImpl v1_0_0 = new MCVersionImpl(1, 0, 0);
        MCVersionImpl v1_0_1 = new MCVersionImpl(1, 0, 1);
        MCVersionImpl v1_1_0 = new MCVersionImpl(1, 1, 0);
        MCVersionImpl v1_1_1 = new MCVersionImpl(1, 1, 1);
        MCVersionImpl v2_0_0 = new MCVersionImpl(2, 0, 0);
        MCVersionImpl v2_0_1 = new MCVersionImpl(2, 0, 1);

        assertTrue(v1_0_0.compareTo(v1_0_1) < 0);
        assertTrue(v1_0_0.compareTo(v1_1_0) < 0);
        assertTrue(v1_0_0.compareTo(v2_0_0) < 0);

        assertTrue(v1_0_1.compareTo(v1_0_0) > 0);
        assertTrue(v1_0_1.compareTo(v1_1_0) < 0);
        assertTrue(v1_0_1.compareTo(v2_0_0) < 0);

        assertTrue(v1_1_0.compareTo(v1_0_0) > 0);
        assertTrue(v1_1_0.compareTo(v1_0_1) > 0);
        assertTrue(v1_1_0.compareTo(v1_1_1) < 0);
        assertTrue(v1_1_0.compareTo(v2_0_0) < 0);

        assertTrue(v1_1_1.compareTo(v1_1_0) > 0);
        assertTrue(v1_1_1.compareTo(v2_0_0) < 0);

        assertTrue(v2_0_1.compareTo(v2_0_0) > 0);
        assertEquals(0, v2_0_1.compareTo(new MCVersionImpl(2, 0, 1)));
    }

    @Test
    public void getFormatted() {
        MCVersion version = MCVersion.of(1, 2, 5);

        assertEquals("1.2.5", version.getFormatted());
    }

    @Test
    public void isHigherThan() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isHigherThan(MCVersion.of(1, 16, 5)));
        assertFalse(version.isHigherThan(MCVersion.of(1, 17, 1)));
    }

    @Test
    public void isLowerThan() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isLowerThan(MCVersion.of(1, 17, 2)));
        assertFalse(version.isLowerThan(MCVersion.of(1, 17, 1)));
    }

    @Test
    public void isHigherOrEqual() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isHigherOrEqual(MCVersion.of(1, 16, 5)));
        assertTrue(version.isHigherOrEqual(MCVersion.of(1, 17, 1)));
        assertFalse(version.isHigherOrEqual(MCVersion.of(1, 17, 2)));
    }

    @Test
    public void isLowerOrEqual() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isLowerOrEqual(MCVersion.of(1, 17, 2)));
        assertTrue(version.isLowerOrEqual(MCVersion.of(1, 17, 1)));
        assertFalse(version.isLowerOrEqual(MCVersion.of(1, 16, 5)));
    }

    @Test
    public void isEqual() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isEqual(MCVersion.of(1, 17, 1)));
        assertFalse(version.isEqual(MCVersion.of(1, 17, 2)));
    }

    @Test
    public void isBetween() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isBetween(MCVersion.of(1, 16, 5), MCVersion.of(1, 17, 2)));
        assertFalse(version.isBetween(MCVersion.of(1, 16, 5), MCVersion.of(1, 17, 0)));
        assertFalse(version.isBetween(MCVersion.of(1, 17, 2), MCVersion.of(1, 17, 3)));
    }

    @Test
    public void isBetween_lowerIsHigher_shouldThrowException() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertThrows(IllegalArgumentException.class, () -> version.isBetween(MCVersion.of(1, 17, 2), MCVersion.of(1, 17, 1)));
    }

    @Test
    public void isBetweenOrEqual() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertTrue(version.isBetweenOrEqual(MCVersion.of(1, 16, 5), MCVersion.of(1, 17, 2)));
        assertTrue(version.isBetweenOrEqual(MCVersion.of(1, 17, 1), MCVersion.of(1, 17, 2)));
        assertTrue(version.isBetweenOrEqual(MCVersion.of(1, 16, 5), MCVersion.of(1, 17, 1)));
        assertFalse(version.isBetweenOrEqual(MCVersion.of(1, 16, 5), MCVersion.of(1, 17, 0)));
        assertFalse(version.isBetweenOrEqual(MCVersion.of(1, 17, 2), MCVersion.of(1, 17, 3)));
        assertFalse(version.isBetweenOrEqual(MCVersion.of(1, 17, 3), MCVersion.of(1, 17, 4)));
    }

    @Test
    public void isBetweenOrEqual_lowerIsHigher_shouldThrowException() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);

        assertThrows(IllegalArgumentException.class, () -> version.isBetweenOrEqual(MCVersion.of(1, 17, 2), MCVersion.of(1, 17, 1)));
    }

    @Test
    public void equalsAndHashcode_shouldBeEqual() {
        MCVersionImpl version = new MCVersionImpl(1, 17, 1);
        MCVersionImpl version2 = new MCVersionImpl(1, 17, 1);

        assertEquals(version, version2);
        assertEquals(version.hashCode(), version2.hashCode());
    }


}
