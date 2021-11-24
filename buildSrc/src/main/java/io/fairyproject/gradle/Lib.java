package io.fairyproject.gradle;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public class Lib {

    @NotNull
    private final String dependency;
    @Nullable
    private final String repository;

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dependency", this.dependency);
        if (this.repository != null)
            jsonObject.addProperty("repository", this.repository);
        return jsonObject;
    }

}
