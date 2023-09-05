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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import io.fairyproject.Fairy;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.log.Log;
import io.fairyproject.mc.util.VersionFormatUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@InjectableComponent
public class MCVersionMappingRegistry {

    private final Gson gson = new Gson();
    private final Map<Integer, MCVersionMapping> mappingByVersion = new HashMap<>();
    private final Map<Integer, MCVersionMapping> mappingByProtocol = new HashMap<>();
    private final TreeSet<Integer> versions = new TreeSet<>();

    @PreInitialize
    public void onPreInitialize() throws IOException {
        try {
            JsonArray jsonElements = this.loadFromInternet();
            this.writeToCacheFile(jsonElements);
        } catch (IOException e) {
            Log.error("Failed to load version mappings from internet, loading from local resources...");
            this.readFromCacheFile();
        }
    }

    private void readFromCacheFile() {
        File dataFolder = Fairy.getPlatform().getDataFolder();
        Path path = new File(dataFolder, "cache-protocol-versions.json").toPath();
        if (!Files.exists(path)) {
            throw new IllegalStateException("No version mappings found in cache file");
        }

        try {
            JsonArray jsonElements = gson.fromJson(new JsonReader(new InputStreamReader(Files.newInputStream(path))), JsonArray.class);
            for (JsonElement element : jsonElements) {
                JsonObject object = element.getAsJsonObject();
                this.loadVersionFromMinecraftData(object);
            }
        } catch (IOException e) {
            Log.error("Failed to read version mappings from file", e);
        }
    }

    private void writeToCacheFile(JsonArray jsonElements) {
        File dataFolder = Fairy.getPlatform().getDataFolder();
        Path path = new File(dataFolder, "cache-protocol-versions.json").toPath();
        try {
            Files.write(path, gson.toJson(jsonElements).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            Log.error("Failed to write version mappings to file", e);
        }
    }

    public MCVersionMapping findMapping(int major, int minor, int patch) {
        int key = VersionFormatUtil.versionToInt(major, minor, patch);
        Integer matchKey = this.versions.floor(key);
        if (matchKey == null)
            throw new IllegalArgumentException("No mapping found for version " + major + "." + minor + "." + patch);
        return mappingByVersion.get(matchKey);
    }

    public MCVersionMapping findMapping(MCVersion mcVersion) {
        return findMapping(mcVersion.getMajor(), mcVersion.getMinor(), mcVersion.getPatch());
    }

    public MCVersionMapping findMappingByProtocol(int protocolVersion) {
        return mappingByProtocol.get(protocolVersion);
    }

    protected void register(MCVersionMapping mapping) {
        int version = VersionFormatUtil.versionToInt(mapping.getMajor(), mapping.getMinor(), mapping.getPatch());
        this.mappingByVersion.put(version, mapping);
        this.mappingByProtocol.put(mapping.getProtocolVersion(), mapping);
        this.versions.add(version);
    }

    public JsonArray loadFromInternet() throws IOException {
        URLConnection urlConnection = new URL("https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/common/protocolVersions.json").openConnection();
        // load the output of the connection into a json array
        JsonArray jsonArray = gson.fromJson(new JsonReader(new InputStreamReader(urlConnection.getInputStream())), JsonArray.class);
        // iterate over the array and register each version
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();
            this.loadVersionFromMinecraftData(object);
        }
    }

    protected void loadVersionFromMinecraftData(JsonObject object) {
        String minecraftVersion = object.get("minecraftVersion").getAsString();
        // filter versions that matches the pattern x.y.z or x.y
        if (!minecraftVersion.matches("\\d+\\.\\d+(\\.\\d+)?"))
            return;

        int protocolVersion = object.get("version").getAsInt();
        // split minecraftVersion to major, minor and patch
        int[] ints = VersionFormatUtil.splitVersionStringToMajorMinorPatch(minecraftVersion);
        int major = ints[0];
        int minor = ints[1];
        int patch = ints[2];

        boolean hexColor = major >= 1 && minor >= 16;
        boolean nmsPrefix = major < 1 || minor < 17;

        this.register(new MCVersionMapping(major, minor, patch, nmsPrefix, hexColor, protocolVersion));
    }

}
