package io.fairyproject.gradle.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import io.fairyproject.gradle.FairyExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileGeneratorFairy implements FileGenerator {
    @Override
    public Pair<String, byte[]> generate(FairyExtension extension, String mainClass) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", extension.getName().get());
        jsonObject.addProperty("mainClass", mainClass);
        jsonObject.addProperty("shadedPackage", extension.getMainPackage().get());

        final List<String> modules = extension.getFairyModules().getOrNull();
        if (modules != null) {
            JsonArray jsonArray = new JsonArray();
            for (String module : modules) {
                jsonArray.add(module);
            }
            jsonObject.add("modules", jsonArray);
        }

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return Pair.of("fairy.json", gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
