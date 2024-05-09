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

package io.fairyproject.bukkit.util;

import io.fairyproject.log.Log;
import io.fairyproject.util.ConditionUtils;
import org.bukkit.Bukkit;

import lombok.Getter;

/**
 * Represents the current Minecraft version the plugin loaded on
 */
public final class MinecraftVersion {

    /**
     * The string representation of the version, for example V1_13
     */
    private static String serverVersion;

    /**
     * The wrapper representation of the version
     */
    @Getter
    private static V current;

    /**
     * The version wrapper
     */
    public enum V {
        v1_16(16, false),
        v1_15(15),
        v1_14(14),
        v1_13(13),
        v1_12(12),
        v1_11(11),
        v1_10(10),
        v1_9(9),
        v1_8(8),
        v1_7(7),
        v1_6(6),
        v1_5(5),
        v1_4(4),
        v1_3_AND_BELOW(3);

        /**
         * The numeric version (the second part of the 1.x number)
         */
        private final int ver;

        /**
         * Is this library tested with this Minecraft version?
         */
        @Getter
        private final boolean tested;

        /**
         * Creates new enum for a MC version that is tested
         *
         * @param version
         */
        V(int version) {
            this(version, true);
        }

        /**
         * Creates new enum for a MC version
         *
         * @param version
         * @param tested
         */
        V(int version, boolean tested) {
            this.ver = version;
            this.tested = tested;
        }

        /**
         * Attempts to get the version from number
         *
         * @throws RuntimeException if number not found
         * @param number
         * @return
         */
        protected static V parse(int number) {
            for (final V v : values())
                if (v.ver == number)
                    return v;

            throw new IllegalStateException("Invalid version number: " + number);
        }
    }

    /**
     * Does the current Minecraft version equal the given version?
     *
     * @param version
     * @return
     */
    public static boolean equals(V version) {
        return compareWith(version) == 0;
    }

    /**
     * Is the current Minecraft version older than the given version?
     *
     * @param version
     * @return
     */
    public static boolean olderThan(V version) {
        return compareWith(version) < 0;
    }

    /**
     * Is the current Minecraft version newer than the given version?
     *
     * @param version
     * @return
     */
    public static boolean newerThan(V version) {
        return compareWith(version) > 0;
    }

    /**
     * Is the current Minecraft version at equals or newer than the given version?
     *
     * @param version
     * @return
     */
    public static boolean atLeast(V version) {
        return equals(version) || newerThan(version);
    }

    // Compares two versions by the number
    private static int compareWith(V version) {
        try {
            return getCurrent().ver - version.ver;

        } catch (final Throwable t) {
            t.printStackTrace();

            return 0;
        }
    }

    /**
     * Return the class versioning such as v1_14_R1
     *
     * @return
     */
    public static String getServerVersion() {
        return serverVersion.equals("craftbukkit") ? "" : serverVersion;
    }

    // Initialize the version
    static {
        try {

            final String packageName = Bukkit.getServer() == null ? "" : Bukkit.getServer().getClass().getPackage().getName();
            final String curr = packageName.substring(packageName.lastIndexOf('.') + 1);
            final boolean hasGatekeeper = !"craftbukkit".equals(curr);

            serverVersion = curr;

            if (hasGatekeeper) {
                int pos = 0;

                for (final char ch : curr.toCharArray()) {
                    pos++;

                    if (pos > 2 && ch == 'R')
                        break;
                }

                final String numericVersion = curr.substring(1, pos - 2).replace("_", ".");

                int found = 0;

                for (final char ch : numericVersion.toCharArray())
                    if (ch == '.')
                        found++;

                ConditionUtils.is(found == 1, "Minecraft Version checker malfunction. Could not detect your server version. Detected: " + numericVersion + " Current: " + curr);

                current = V.parse(Integer.parseInt(numericVersion.split("\\.")[1]));

            } else
                current = V.v1_3_AND_BELOW;

        } catch (final Throwable t) {
            Log.error("Error detecting your Minecraft version. Check your server compatibility.", t);
        }
    }
}