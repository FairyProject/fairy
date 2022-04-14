package io.fairyproject.gradle.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.FairyBuildData;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Project;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileGeneratorFairy implements FileGenerator {
    @Override
    public Pair<String, byte[]> generate(Project project, FairyBuildData extension, String mainClass, List<String> otherModules) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", extension.getName());
        if (mainClass != null) {
            jsonObject.addProperty("mainClass", mainClass);
            jsonObject.addProperty("shadedPackage", extension.getMainPackage());
        }

        JsonArray jsonArray = new JsonArray();
//        final Map<String, String> modules = extension.getFairyModules();
//        if (modules != null) {
//            for (Map.Entry<String, String> module : modules.entrySet()) {
//                jsonArray.add(module.getKey() + ":" + module.getValue());
//            }
//        }
//        for (Map.Entry<String, String> entry : otherModules.entrySet()) {
//            jsonArray.add(entry.getKey() + ":" + entry.getValue());
//        }
        jsonObject.add("modules", jsonArray);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return Pair.of("fairy.json", gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
