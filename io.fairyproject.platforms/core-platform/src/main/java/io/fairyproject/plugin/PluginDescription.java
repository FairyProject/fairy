package io.fairyproject.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.library.Library;
import io.fairyproject.util.ConditionUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class PluginDescription {

    private final String name;
    private final String mainClass;
    private final String shadedPackage;
    @Singular
    private final List<Library> libraries;

    public PluginDescription(JsonObject jsonObject) {
        ConditionUtils.check(jsonObject.has("name"), "name property could not be found.");
        ConditionUtils.check(jsonObject.has("mainClass"), "mainClass property could not be found.");
        ConditionUtils.check(jsonObject.has("shadedPackage"), "shadedPackage property could not be found.");

        this.name = jsonObject.get("name").getAsString();
        this.mainClass = jsonObject.get("mainClass").getAsString();
        this.shadedPackage = jsonObject.get("shadedPackage").getAsString();

        this.libraries = new ArrayList<>();
        if (jsonObject.has("libraries")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("libraries")) {
                this.libraries.add(Library.fromJsonObject(jsonElement.getAsJsonObject(), this.shadedPackage));
            }
        }
    }

}
