package io.fairyproject.plugin;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.util.entry.EntryArrayList;
import lombok.Data;
import io.fairyproject.library.Library;

import java.util.ArrayList;
import java.util.List;

@Data
public class PluginDescription {

    private final String name;
    private final String mainClass;
    private final String shadedPackage;
    private final EntryArrayList<String, String> modules;
    private final List<String> extensions;
    private final List<Library> libraries;

    public PluginDescription(JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject.has("name"), "name property could not be found.");
        Preconditions.checkArgument(jsonObject.has("mainClass"), "mainClass property could not be found.");
        Preconditions.checkArgument(jsonObject.has("shadedPackage"), "shadedPackage property could not be found.");

        this.name = jsonObject.get("name").getAsString();
        this.mainClass = jsonObject.get("mainClass").getAsString();
        this.shadedPackage = jsonObject.get("shadedPackage").getAsString();

        this.modules = new EntryArrayList<>();
        if (jsonObject.has("modules")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("modules")) {
                final String[] entry = jsonElement.getAsString().split(":");
                this.modules.add(entry[0], entry[1]);
            }
        }

        this.extensions = new ArrayList<>();
        if (jsonObject.has("extensions")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("extensions")) {
                this.extensions.add(jsonElement.getAsString());
            }
        }

        this.libraries = new ArrayList<>();
        if (jsonObject.has("libraries")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("libraries")) {
                this.libraries.add(Library.fromJsonObject(jsonElement.getAsJsonObject(), this.shadedPackage));
            }
        }
    }

}
