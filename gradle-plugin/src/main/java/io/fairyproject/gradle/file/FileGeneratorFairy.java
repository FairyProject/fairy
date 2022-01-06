package io.fairyproject.gradle.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.FairyExtension;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FileGeneratorFairy implements FileGenerator {
    @Override
    public Pair<String, byte[]> generate(FairyExtension extension, String mainClass, Map<String, String> otherModules) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", extension.getName().get());
        if (mainClass != null) {
            jsonObject.addProperty("mainClass", mainClass);
            jsonObject.addProperty("shadedPackage", extension.getMainPackage().get());
        }

        JsonArray jsonArray = new JsonArray();
        final Map<String, String> modules = extension.getFairyModules();
        if (modules != null) {
            for (Map.Entry<String, String> module : modules.entrySet()) {
                jsonArray.add(module.getKey() + ":" + module.getValue());
            }
        }
        for (Map.Entry<String, String> entry : otherModules.entrySet()) {
            jsonArray.add(entry.getKey() + ":" + entry.getValue());
        }
        jsonObject.add("modules", jsonArray);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return Pair.of("fairy.json", gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
