package org.fairy.gradle.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.fairy.gradle.FairyExtension;

import java.nio.charset.StandardCharsets;

public class FileGeneratorFairy implements FileGenerator {
    @Override
    public Pair<String, byte[]> generate(FairyExtension extension, String mainClass) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", extension.getName().get());
        jsonObject.addProperty("mainClass", mainClass);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return Pair.of("fairy.json", gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
