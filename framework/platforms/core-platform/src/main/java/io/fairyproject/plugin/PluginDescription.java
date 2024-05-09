package io.fairyproject.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.library.Library;
import io.fairyproject.util.ConditionUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class PluginDescription {

    private final String name;
    private final String mainClass;
    private final String shadedPackage;
    private final String fairyPackage;
    @Singular
    private final List<Library> libraries;

    public PluginDescription(JsonObject jsonObject) {
        ConditionUtils.is(jsonObject.has("name"), "name property could not be found.");
        ConditionUtils.is(jsonObject.has("mainClass"), "mainClass property could not be found.");
        ConditionUtils.is(jsonObject.has("shadedPackage"), "shadedPackage property could not be found.");

        this.name = jsonObject.get("name").getAsString();
        this.mainClass = jsonObject.get("mainClass").getAsString();
        this.shadedPackage = jsonObject.get("shadedPackage").getAsString();
        if (jsonObject.has("fairyPackage")) {
            this.fairyPackage = jsonObject.get("fairyPackage").getAsString();
        } else {
            this.fairyPackage = this.shadedPackage + ".fairy";
        }

        this.libraries = new ArrayList<>();
        if (jsonObject.has("libraries")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("libraries")) {
                this.libraries.add(Library.fromJsonObject(jsonElement.getAsJsonObject()));
            }
        }
    }

}
