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

package io.fairyproject.mc.version.impl;

import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.util.ConditionUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public class MCVersionImpl implements MCVersion {

    private final int major;
    private final int minor;
    private final int patch;

    @Override
    public String getFormatted() {
        if (patch > 0) {
            return String.format("%d.%d.%d", major, minor, patch);
        }
        return String.format("%d.%d", major, minor);
    }

    @Override
    public boolean isHigherThan(MCVersion version) {
        return this.compareTo(version) > 0;
    }

    @Override
    public boolean isHigherOrEqual(MCVersion version) {
        return this.compareTo(version) >= 0;
    }

    @Override
    public boolean isLowerThan(MCVersion version) {
        return this.compareTo(version) < 0;
    }

    @Override
    public boolean isLowerOrEqual(MCVersion version) {
        return this.compareTo(version) <= 0;
    }

    @Override
    public boolean isEqual(MCVersion version) {
        return this.compareTo(version) == 0;
    }

    @Override
    public boolean isBetween(MCVersion lower, MCVersion higher) {
        ConditionUtils.not(lower.isHigherThan(higher), "Lower version cannot be higher than higher version");
        return this.isHigherThan(lower) && this.isLowerThan(higher);
    }

    @Override
    public boolean isBetweenOrEqual(MCVersion lower, MCVersion higher) {
        ConditionUtils.not(lower.isHigherThan(higher), "Lower version cannot be higher than higher version");
        return this.isHigherOrEqual(lower) && this.isLowerOrEqual(higher);
    }

    @Override
    public int compareTo(@NotNull MCVersion mcVersion) {
        if (this.getMajor() != mcVersion.getMajor())
            return Integer.compare(this.getMajor(), mcVersion.getMajor());

        if (this.getMinor() != mcVersion.getMinor())
            return Integer.compare(this.getMinor(), mcVersion.getMinor());

        return Integer.compare(this.getPatch(), mcVersion.getPatch());
    }

}
