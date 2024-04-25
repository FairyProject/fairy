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

public interface MCVersion extends Comparable<MCVersion> {

    static MCVersion of(int major, int minor, int patch) {
        return new MCVersionImpl(major, minor, patch);
    }

    static MCVersion of(int minor, int patch) {
        return of(1, minor, patch);
    }

    static MCVersion of(int minor) {
        return of(1, minor, 0);
    }

    int getMajor();

    int getMinor();

    int getPatch();

    String getFormatted();

    boolean isHigherThan(MCVersion version);

    boolean isHigherOrEqual(MCVersion version);

    boolean isLowerThan(MCVersion version);

    boolean isLowerOrEqual(MCVersion version);

    boolean isEqual(MCVersion version);

    boolean isBetween(MCVersion lower, MCVersion higher);

    boolean isBetweenOrEqual(MCVersion lower, MCVersion higher);

}
