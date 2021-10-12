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

package org.fairy.bukkit.reflection.version;

import java.util.Arrays;

public enum PlayerVersion {
    v1_7(4, 5),
    v1_8(47),
    v1_9(107, 108, 109, 110),
    v1_10(210),
    v1_11(315, 316),
    v1_12(335, 338, 340),
    v1_13(393, 401, 404);

    private Integer[] rawVersion;

    PlayerVersion(Integer... rawVersionNumbers) {
        this.rawVersion = rawVersionNumbers;
    }

    public static PlayerVersion getVersionFromRaw(Integer input) {
        PlayerVersion[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            PlayerVersion playerVersion = var1[var3];
            if (Arrays.asList(playerVersion.rawVersion).contains(input)) {
                return playerVersion;
            }
        }

        return v1_8;
    }

    public Integer[] getRawVersion() {
        return this.rawVersion;
    }
}

