package io.fairyproject.mc.version.cache;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import io.fairyproject.Fairy;
import io.fairyproject.log.Log;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
        URLConnection urlConnection = new URL("https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/common/protocolVersions.json").openConnection();
        // load the output of the connection into a json array
        return gson.fromJson(new JsonReader(new InputStreamReader(urlConnection.getInputStream())), JsonArray.class);
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
