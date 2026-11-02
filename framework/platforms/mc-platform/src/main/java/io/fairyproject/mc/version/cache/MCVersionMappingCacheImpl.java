package io.fairyproject.mc.version.cache;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import io.fairyproject.Fairy;
import io.fairyproject.log.Log;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class MCVersionMappingCacheImpl implements MCVersionMappingCache {

    private final Gson gson;

    @Override
    public JsonArray read() {
        File dataFolder = Fairy.getPlatform().getDataFolder();
        Path path = new File(dataFolder, "cache-protocol-versions.json").toPath();
        if (!Files.exists(path)) {
            return null;
        }

        try {
            return gson.fromJson(new JsonReader(new InputStreamReader(Files.newInputStream(path))), JsonArray.class);
        } catch (IOException e) {
            Log.error("Failed to read version mappings from file", e);
        }

        return null;
    }

    @Override
    public @NotNull JsonArray load() throws IOException {
        String url = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/common/protocolVersions.json";

        try {
            URLConnection urlConnection = new URL(url).openConnection();
            // load the output of the connection into a json array
            return gson.fromJson(new JsonReader(new InputStreamReader(urlConnection.getInputStream())), JsonArray.class);
        } catch (UnknownHostException e) {
            io.fairyproject.log.Log.error("Unable to locate raw.githubusercontent.com, please check your internet connection.");
            io.fairyproject.log.Log.error("If you are behind a proxy, please make sure to configure it properly.");
            io.fairyproject.log.Log.error("If you are using a firewall, please make sure to whitelist the connection.");
            io.fairyproject.log.Log.error("Fairy is trying to load " + url + " to get the latest protocol versions, please whitelist it at least once.");

            SneakyThrowUtil.sneakyThrow(e);
            return null;
        }
    }

    @Override
    public void write(@NotNull JsonArray jsonElements) {
        File dataFolder = Fairy.getPlatform().getDataFolder();
        Path path = new File(dataFolder, "cache-protocol-versions.json").toPath();
        try {
            if (Files.exists(path))
                Files.delete(path);
            Files.write(path, gson.toJson(jsonElements).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            Log.error("Failed to write version mappings to file", e);
        }
    }
}
