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

package io.fairyproject.mc.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionFormatUtilTest {

    @Test
    public void versionToIntTest() {
        Assertions.assertEquals(10000, VersionFormatUtil.versionToInt("1.0.0"));
        Assertions.assertEquals(10001, VersionFormatUtil.versionToInt("1.0.1"));
        Assertions.assertEquals(10100, VersionFormatUtil.versionToInt("1.1.0"));
        Assertions.assertEquals(10101, VersionFormatUtil.versionToInt("1.1.1"));
        Assertions.assertEquals(11000, VersionFormatUtil.versionToInt("1.10.0"));
        Assertions.assertEquals(11001, VersionFormatUtil.versionToInt("1.10.1"));
        Assertions.assertEquals(11100, VersionFormatUtil.versionToInt("1.11.0"));
        Assertions.assertEquals(11101, VersionFormatUtil.versionToInt("1.11.1"));
        Assertions.assertEquals(20000, VersionFormatUtil.versionToInt("2.0.0"));
    }

    @Test
    public void versionToIntWithStringTest() {
        Assertions.assertEquals(10000, VersionFormatUtil.versionToInt("1.0.0"));
        Assertions.assertEquals(11001, VersionFormatUtil.versionToInt("1.10.1"));
        Assertions.assertEquals(20000, VersionFormatUtil.versionToInt("2.0.0"));
        Assertions.assertEquals(21000, VersionFormatUtil.versionToInt("2.10"));
    }

    @Test
    public void splitVersionStringToMajorMinorPatch_shouldReturnCorrectValues() {
        Assertions.assertArrayEquals(new int[]{1, 0, 0}, VersionFormatUtil.splitVersionStringToMajorMinorPatch("1.0.0"));
        Assertions.assertArrayEquals(new int[]{1, 10, 1}, VersionFormatUtil.splitVersionStringToMajorMinorPatch("1.10.1"));
        Assertions.assertArrayEquals(new int[]{2, 0, 0}, VersionFormatUtil.splitVersionStringToMajorMinorPatch("2.0.0"));
        Assertions.assertArrayEquals(new int[]{2, 10, 0}, VersionFormatUtil.splitVersionStringToMajorMinorPatch("2.10"));
    }
    
}
