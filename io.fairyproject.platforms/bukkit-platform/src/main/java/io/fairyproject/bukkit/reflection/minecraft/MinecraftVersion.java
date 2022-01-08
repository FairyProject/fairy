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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;

public class MinecraftVersion {

    private static final Logger LOGGER = LogManager.getLogger(MinecraftVersion.class);

    public static MinecraftVersion VERSION;

    static {
        LOGGER.info("I am loaded from package " + MinecraftReflection.class.getPackage().getName());
        try {
            VERSION = MinecraftVersion.getVersion();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get version", e);
        }

        if (VERSION != null)
            LOGGER.info("Version is " + VERSION);
        else LOGGER.warn("Failed to recognise the version");
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

    public static MinecraftVersion getVersion() {
        Class serverClass;
        try {
            serverClass = Bukkit.getServer().getClass();
        } catch (Exception e) {
            LOGGER.error("Failed to get bukkit server class: ", e);
            LOGGER.error("Assuming we're in a test environment!");
            return null;
        }

        String name = serverClass.getPackage().getName();
        if (name.equals("be.seeseemelk.mockbukkit")) {
            System.err.println("MockBukkit found! we are in test environment! we will let developer take care of version setting...");
            return null;
        }

        String versionPackage = name.substring(name.lastIndexOf('.') + 1);
        for (MinecraftReflection.Version version : MinecraftReflection.Version.values()) {
            MinecraftVersion minecraftVersion = version.minecraft();
            if (minecraftVersion.matchesPackageName(versionPackage)) {
                return minecraftVersion;
            }
        }
        LOGGER.error("Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

        LOGGER.info("Generating dynamic constant...");
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
            LOGGER.info("Injected dynamic version " + packageName + " (#" + numVersion + ").");
            LOGGER.info("Please inform inventivetalent about the outdated version, as this is not guaranteed to work.");
            return new MinecraftVersion(packageName, numVersion);
        }

        LOGGER.error("Failed to create dynamic version for " + versionPackage);

        return new MinecraftVersion("UNKNOWN", -1);
    }
}
