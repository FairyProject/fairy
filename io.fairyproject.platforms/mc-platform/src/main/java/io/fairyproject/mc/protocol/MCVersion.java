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

package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public enum MCVersion {
    V1_7(4, 5),
    V1_8(47),
    V1_9(107, 108, 109, 110),
    V1_10(210),
    V1_11(315, 316),
    V1_12(335, 338, 340),
    V1_13(393, 401, 404),
    V1_14(477, 480, 485, 490, 498),
    V1_15(573, 575, 578),
    V1_16(735, 736, 751, 753, 754),
    V1_17(true, true, 755, 756),
    V1_18(true, true, 757, 758),
    V1_19(true, true, 759, 760);

    private final int[] rawVersion;
    private final boolean hexColorSupport;
    private final boolean nmsPrefix;

    MCVersion(int... rawVersionNumbers) {
        this(false, rawVersionNumbers);
    }

    MCVersion(boolean hexColorSupport, int... rawVersionNumbers) {
        this(hexColorSupport, true, rawVersionNumbers);
    }

    MCVersion(boolean hexColorSupport, boolean nmsPrefix, int... rawVersionNumbers) {
        this.rawVersion = rawVersionNumbers;
        this.nmsPrefix = nmsPrefix;
        this.hexColorSupport = hexColorSupport;
    }

    public boolean isOrAbove(MCVersion version) {
        return this.ordinal() >= version.ordinal();
    }

    public boolean isOrBelow(MCVersion version) {
        return this.ordinal() <= version.ordinal();
    }

    public boolean above(MCVersion version) {
        return this.ordinal() > version.ordinal();
    }

    public boolean below(MCVersion version) {
        return this.ordinal() < version.ordinal();
    }

    public ClientVersion toClientVersion() {
        return ClientVersion.getById(this.rawVersion[0]);
    }

    public static MCVersion getVersionFromRaw(int input) {
        for (MCVersion mcVersion : values()) {
            if (ArrayUtils.contains(mcVersion.rawVersion, input)) {
                return mcVersion;
            }
        }

        return V1_8;
    }
}

