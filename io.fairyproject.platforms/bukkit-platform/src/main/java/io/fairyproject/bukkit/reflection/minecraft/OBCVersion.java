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

package io.fairyproject.bukkit.reflection.minecraft;

import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import org.bukkit.Bukkit;

public enum OBCVersion {

    UNKNOWN(true) {
        @Override
        public boolean matchesPackageName(String packageName) {
            return false;
        }
    },

    v1_7_R1(true),
    v1_7_R2(true),
    v1_7_R3(true),
    v1_7_R4(true),

    v1_8_R1(true),
    v1_8_R2(true),
    v1_8_R3(true),
    //Does this even exists?
    v1_8_R4(true),

    v1_9_R1(true),
    v1_9_R2(true),

    v1_10_R1(true),

    v1_11_R1(true),

    v1_12_R1(true),

    v1_13_R1(true),
    v1_13_R2(true),

    v1_14_R1(true),

    v1_15_R1(true),

    v1_16_R1(true),
    v1_16_R2(true),
    v1_16_R3(true),

    v1_17_R1(false),

    v1_18_R1(false),
    v1_18_R2(false),

    /// (Potentially) Upcoming versions
    v1_19_R1(false),

    v1_20_R1(false);

    private static OBCVersion VERSION;

    private final String packageName;
    private final String nmsPackage;
    private final String obcPackage;
    private final boolean nmsVersionPrefix;

    private MCVersion mcVersion;

    public static OBCVersion get() {
        if (VERSION == null) {
            try {
                VERSION = OBCVersion.getVersion();
            } catch (Exception e) {
                SneakyThrowUtil.sneakyThrow(e);
            }
        }
        return VERSION;
    }

    public static void forceSet(OBCVersion version) {
        VERSION = version;
    }

    OBCVersion(boolean nmsVersionPrefix) {
        this.packageName = this.name();
        if (nmsVersionPrefix) {
            this.nmsPackage = String.format("net.minecraft.server.%s", packageName);
            this.obcPackage = String.format("org.bukkit.craftbukkit.%s", packageName);
            this.nmsVersionPrefix = true;
        } else {
            this.nmsPackage = "net.minecraft";
            this.obcPackage = String.format("org.bukkit.craftbukkit.%s", packageName);
            this.nmsVersionPrefix = false;
        }
    }

    /**
     * @deprecated use {@link #getNmsPackage()} / {@link #getObcPackage()} instead
     */
    @Deprecated
    public String packageName() {
        return packageName;
    }

    /**
     * @return the full package name for net.minecraft....
     */
    public String getNmsPackage() {
        return nmsPackage;
    }

    /**
     * @return the full package name for org.bukkit....
     */
    public String getObcPackage() {
        return obcPackage;
    }

    public boolean isOrAbove(MCVersion version) {
        return this.toMCVersion().ordinal() >= version.ordinal();
    }

    public boolean isOrBelow(MCVersion version) {
        return this.toMCVersion().ordinal() <= version.ordinal();
    }

    public boolean above(MCVersion version) {
        return this.toMCVersion().ordinal() > version.ordinal();
    }

    public boolean below(MCVersion version) {
        return this.toMCVersion().ordinal() < version.ordinal();
    }

    public boolean isOrAbove(OBCVersion version) {
        return this.ordinal() >= version.ordinal();
    }

    public boolean isOrBelow(OBCVersion version) {
        return this.ordinal() <= version.ordinal();
    }

    public boolean above(OBCVersion version) {
        return this.ordinal() > version.ordinal();
    }

    public boolean below(OBCVersion version) {
        return this.ordinal() < version.ordinal();
    }

    public MCVersion toMCVersion() {
        if (this.mcVersion == null) {
            final String s = this.name().toUpperCase();
            this.mcVersion = MCVersion.valueOf(s.substring(0, s.lastIndexOf("_")));
        }
        return this.mcVersion;
    }

    /**
     * @return if the nms package name has version prefix
     */
    public boolean hasNMSVersionPrefix() {
        return nmsVersionPrefix;
    }

    public boolean matchesPackageName(String packageName) {
        return this.packageName.toLowerCase().contains(packageName.toLowerCase());
    }

    @Override
    public String toString() {
        return packageName;
    }

    private static OBCVersion getVersion() {
        Class<?> serverClass;
        try {
            serverClass = Bukkit.getServer().getClass();
        } catch (Exception e) {
            System.err.println("[Fairy] Failed to get bukkit server class: " + e.getMessage());
            System.err.println("[Fairy] Assuming we're in a test environment!");
            return OBCVersion.v1_16_R3;
        }

        String name = serverClass.getPackage().getName();
        if (name.equals("be.seeseemelk.mockbukkit")) {
            System.err.println("[Fairy] MockBukkit found! we are in test environment! we will let developer take care of version setting...");
            return null;
        }
        String versionPackage = name.substring(name.lastIndexOf('.') + 1);
        for (OBCVersion version : OBCVersion.values()) {
            if (version.matchesPackageName(versionPackage)) {
                return version;
            }
        }
        System.err.println("[Fairy] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");
        return OBCVersion.v1_16_R3;
    }
}
