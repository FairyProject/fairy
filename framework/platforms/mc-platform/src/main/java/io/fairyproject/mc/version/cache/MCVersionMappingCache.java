package io.fairyproject.mc.version.cache;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface MCVersionMappingCache {

    /**
     * Read the cache file and return the JsonObject
     *
     * @return the JsonObject read from the cache file, or null if the file does not exist
     */
    @Nullable
    JsonArray read();

    /**
     * Load from internet and return the JsonObject
     *
     * @return the JsonObject loaded from the internet
     */
    @NotNull
    JsonArray load() throws IOException;

    /**
     * Write the JsonObject to the cache file
     *
     * @param jsonArray the JsonObject to write
     */
    void write(@NotNull JsonArray jsonArray);

}
