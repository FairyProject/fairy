package io.fairyproject.gradle.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.FairyBuildData;
import io.fairyproject.gradle.FairyPlugin;
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
        extension.getLibraries().forEach(lib -> jsonArray.add(lib.toJsonObject()));
        jsonObject.add("libraries", jsonArray);

        return Pair.of("fairy.json", FairyPlugin.GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
