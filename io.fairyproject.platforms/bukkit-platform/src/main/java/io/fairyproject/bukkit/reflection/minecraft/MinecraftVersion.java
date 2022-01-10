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

import io.fairyproject.bukkit.reflection.MinecraftReflection;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;

public class MinecraftVersion {

    private static MinecraftVersion VERSION;

    public static MinecraftVersion get() {
        if (VERSION == null) {
            System.out.println("[Imanity/MinecraftVersion] I am loaded from package " + MinecraftReflection.class.getPackage().getName());
            try {
                VERSION = MinecraftVersion.getVersion();
            } catch (Exception e) {
                throw new RuntimeException("Failed to get version", e);
            }
            if (VERSION != null) {
                System.out.println("[Imanity/MinecraftVersion] Version is " + VERSION);
            }
        }
        return VERSION;
    }

    public static void forceSet(MinecraftVersion version) {
        VERSION = version;
    }

    private final String packageName;
    private final int version;
    private final String nmsPackage;
    private final String obcPackage;
    private final boolean nmsVersionPrefix;
    private MinecraftReflection.Version versionEnum;

    public MinecraftVersion(String packageName, int version, String nmsFormat, String obcFormat, boolean nmsVersionPrefix) {
        this.packageName = packageName;
        this.version = version;
        this.nmsPackage = String.format(nmsFormat, packageName);
        this.obcPackage = String.format(obcFormat, packageName);
        this.nmsVersionPrefix = nmsVersionPrefix;
    }

    public MinecraftVersion(String packageName, int version) {
        this(packageName, version, "net.minecraft.server.%s", "org.bukkit.craftbukkit.%s", true);
    }

    // Used by SantiyCheck
    public MinecraftVersion(MinecraftReflection.Version version) {
        this(version.name(), version.version());
    }

    /**
     * @return the version-number
     */
    public int version() {
        return version;
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

    /**
     * @return if the nms package name has version prefix
     */
    public boolean hasNMSVersionPrefix() {
        return nmsVersionPrefix;
    }

    /**
     * @param version the version to check
     * @return <code>true</code> if this version is older than the specified version
     */
    public boolean olderThan(MinecraftReflection.Version version) {
        return version() < version.version();
    }

    /**
     * @param version the version to check
     * @return <code>true</code> if this version is equals than the specified version
     */
    public boolean equal(MinecraftReflection.Version version) {
        return version() < version.version();
    }

    /**
     * @param version the version to check
     * @return <code>true</code> if this version is newer than the specified version
     */
    public boolean newerThan(MinecraftReflection.Version version) {
        return version() >= version.version();
    }

    /**
     * @param oldVersion The older version to check
     * @param newVersion The newer version to check
     * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
     */
    public boolean inRange(MinecraftReflection.Version oldVersion, MinecraftReflection.Version newVersion) {
        return newerThan(oldVersion) && olderThan(newVersion);
    }

    public boolean matchesPackageName(String packageName) {
        return this.packageName.toLowerCase().contains(packageName.toLowerCase());
    }

    public MinecraftReflection.Version versionEnum() {
        if (versionEnum == null) {
            versionEnum = MinecraftReflection.Version.valueOf(this.packageName);
        }
        return versionEnum;
    }

    @Override
    public String toString() {
        return packageName + " (" + version() + ")";
    }

    private static MinecraftVersion getVersion() {
        Class serverClass;
        try {
            serverClass = Bukkit.getServer().getClass();
        } catch (Exception e) {
            System.err.println("[Imanity/MinecraftVersion] Failed to get bukkit server class: " + e.getMessage());
            System.err.println("[Imanity/MinecraftVersion] Assuming we're in a test environment!");
            return new MinecraftVersion("v1_16_R3", 11603);
        }

        String name = serverClass.getPackage().getName();
        if (name.equals("be.seeseemelk.mockbukkit")) {
            System.err.println("[Imanity/MinecraftVersion] MockBukkit found! we are in test environment! we will let developer take care of version setting...");
            return null;
        }
        String versionPackage = name.substring(name.lastIndexOf('.') + 1);
        for (MinecraftReflection.Version version : MinecraftReflection.Version.values()) {
            MinecraftVersion minecraftVersion = version.minecraft();
            if (minecraftVersion.matchesPackageName(versionPackage)) {
                return minecraftVersion;
            }
        }
        System.err.println("[Imanity/MinecraftVersion] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

        System.out.println("[Imanity/MinecraftVersion] Generating dynamic constant...");
        Matcher matcher = MinecraftReflection.NUMERIC_VERSION_PATTERN.matcher(versionPackage);
        while (matcher.find()) {
            if (matcher.groupCount() < 3) {
                continue;
            }

            String majorString = matcher.group(1);
            String minorString = matcher.group(2);
            if (minorString.length() == 1) {
                minorString = "0" + minorString;
            }
            String patchString = matcher.group(3);
            if (patchString.length() == 1) {
                patchString = "0" + patchString;
            }

            String numVersionString = majorString + minorString + patchString;
            int numVersion = Integer.parseInt(numVersionString);
            String packageName = "v" + versionPackage.substring(1).toUpperCase();

            //dynamic register version
            System.out.println("[Imanity/MinecraftVersion] Injected dynamic version " + packageName + " (#" + numVersion + ").");
            System.out.println("[Imanity/MinecraftVersion] Please inform inventivetalent about the outdated version, as this is not guaranteed to work.");
            return new MinecraftVersion(packageName, numVersion);
        }

        System.err.println("[Imanity/MinecraftVersion] Failed to create dynamic version for " + versionPackage);

        return new MinecraftVersion("v1_16_R3", 11603);
    }
}
