/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.fairy.mc.protocol;

import org.apache.commons.lang3.ArrayUtils;

public enum MCVersion {
    v1_7(4, 5),
    v1_8(47),
    v1_9(107, 108, 109, 110),
    v1_10(210),
    v1_11(315, 316),
    v1_12(335, 338, 340),
    v1_13(393, 401, 404),
    V1_14(477, 480, 485, 490, 498),
    V1_15(573, 575, 578),
    V1_16(735, 736, 751, 753, 754),
    V1_17(755, 756);

    private final int[] rawVersion;

    MCVersion(int... rawVersionNumbers) {
        this.rawVersion = rawVersionNumbers;
    }

    public static MCVersion getVersionFromRaw(int input) {
        for (MCVersion mcVersion : values()) {
            if (ArrayUtils.contains(mcVersion.rawVersion, input)) {
                return mcVersion;
            }
        }

        return v1_8;
    }

    public int[] getRawVersion() {
        return this.rawVersion;
    }
}

